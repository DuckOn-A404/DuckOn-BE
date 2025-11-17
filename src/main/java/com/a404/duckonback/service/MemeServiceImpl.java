package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.*;
import com.a404.duckonback.entity.Meme;
import com.a404.duckonback.entity.MemeTag;
import com.a404.duckonback.entity.Tag;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemeServiceImpl implements MemeService {

    private final MemeS3Service memeS3Service;
    private final MemeRepository memeRepository;
    private final TagRepository tagRepository;
    private final MemeTagRepository memeTagRepository;
    private final UserRepository userRepository;
    private final MemeFavoriteRepository memeFavoriteRepository;
    private final MemeHourlyTop10Repository memeHourlyTop10Repository;
    private final SearchService searchService;
    private final S3ValidationService s3ValidationService;


    @Override
    @Transactional(readOnly = true)
    public MemeResponseDTO getRandomMemes(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);

        long totalCount = memeRepository.count();

        // 아무 밈도 없을 때
        if (totalCount == 0) {
            return MemeResponseDTO.builder()
                    .page(safePage)
                    .size(safeSize)
                    .total(0)
                    .items(Collections.emptyList())
                    .build();
        }

        // 최대 페이지 넘으면 마지막 페이지로 보정
        long maxPage = (totalCount - 1) / safeSize + 1;
        if (safePage > maxPage) {
            safePage = (int) maxPage;
        }

        // 기본 페이지네이션 조회
        var pageable = org.springframework.data.domain.PageRequest.of(safePage - 1, safeSize);
        var memePage = memeRepository.findAll(pageable);

        // 해당 페이지 내에서만 랜덤 셔플 (호출할 때마다 순서 랜덤)
        List<Meme> memes = new ArrayList<>(memePage.getContent());
        java.util.Collections.shuffle(memes);

        // 엔티티 -> DTO 변환
        List<MemeItemDTO> items = memes.stream()
                .map(meme -> {
                    return MemeItemDTO.builder()
                            .memeId(meme.getId())
                            .memeUrl(meme.getImageUrl())
                            .build();
                })
                .toList();

        return MemeResponseDTO.builder()
                .page(safePage)
                .size(safeSize)
                .total((int) totalCount)
                .items(items)
                .build();
    }

    @Override
    public MemeCreateResponseDTO createMemes(Long userId, MemeCreateRequestDTO req) {
        User creator = userRepository.getReferenceById(userId);

        List<MemeCreateResponseDTO.MemeInfoDTO> resultList = new ArrayList<>();

        handleOne(creator, req.getImage1(), req.getTags1()).ifPresent(resultList::add);
        handleOne(creator, req.getImage2(), req.getTags2()).ifPresent(resultList::add);
        handleOne(creator, req.getImage3(), req.getTags3()).ifPresent(resultList::add);

        if (resultList.isEmpty()) {
            throw new IllegalArgumentException("업로드할 밈 이미지가 최소 1개 이상 필요합니다.");
        }

        return MemeCreateResponseDTO.builder()
                .memes(resultList)
                .build();
    }

    private Optional<MemeCreateResponseDTO.MemeInfoDTO> handleOne(
            User creator,
            MultipartFile file,
            Set<String> rawTags
    ) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }

        // 1) S3 업로드
        var upload = memeS3Service.uploadMeme(file);

        // 2) Meme 저장
        Meme meme = Meme.builder()
                .creator(creator)
                .imageUrl(upload.getCdnUrl())
                .usageCnt(0)
                .downloadCnt(0)
                .createdAt(LocalDateTime.now())
                .build();

        meme = memeRepository.save(meme);

        // 3) 태그 정리
        LinkedHashSet<String> normalizedTags = Optional.ofNullable(rawTags)
                .orElse(Collections.emptySet())
                .stream()
                .map(t -> t == null ? "" : t.trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String tagName : normalizedTags) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .tagName(tagName)
                            .build()));

            MemeTag mt = MemeTag.of(meme, tag);
            memeTagRepository.save(mt);
        }

        log.info("✅ Meme created: id={}, url={}", meme.getId(), meme.getImageUrl());

        // 4) 응답용 DTO 생성 (프론트와 1:1 매칭)
        MemeCreateResponseDTO.MemeInfoDTO dto = MemeCreateResponseDTO.MemeInfoDTO.builder()
        .memeId(meme.getId())
        .imageUrl(meme.getImageUrl())
        .tags(new ArrayList<>(normalizedTags))
        .build();

        try {
        // 1) S3에 실제로 존재하는지 확인
        log.info("🔍 S3 존재 여부 확인 시작: key={}", upload.getKey());
        boolean existsInS3 = s3ValidationService.existsInS3(upload.getKey());
        log.info("🔍 S3 존재 여부 확인 결과: key={}, exists={}", upload.getKey(), existsInS3);

        if (existsInS3) {
                // 2) ImageDocument 생성
                ImageDocument imageDocument = ImageDocument.builder()
                        .s3_url(upload.getCdnUrl())
                        .object_key(upload.getKey())
                        .tags(new ArrayList<>(normalizedTags))
                        .created_at(LocalDateTime.now())
                        .build();

                log.info("📦 ImageDocument 생성 완료: s3_url={}, object_key={}, tags={}",
                        imageDocument.getS3_url(),
                        imageDocument.getObject_key(),
                        imageDocument.getTags());

                // 3) OpenSearch에 저장
                searchService.indexImage(imageDocument);

                log.info("✅ Indexed to OpenSearch: objectKey={}, tags={}", upload.getKey(), normalizedTags);

        } else {
                log.warn("⚠️ S3 object not found, skipping OpenSearch indexing: {}", upload.getKey());
        }

        } catch (Exception e) {
        // OpenSearch 저장 실패 시 로그만 남기고 계속 진행
        log.error("❌ OpenSearch indexing failed: objectKey={}, tags={}, error={}",
                upload.getKey(), normalizedTags, e.getMessage(), e);
        // TODO: 나중에 재시도 큐 구현 시 여기에 추가
        }

        return Optional.of(dto);
    }


    public void createFavorite(Long userId, Long memeId){
        if (memeFavoriteRepository.existsByUser_IdAndMeme_Id(userId, memeId)) {
            return;
        }

        // 존재 유효성 체크
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND));
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new CustomException("존재하지 않는 밈 입니다.", HttpStatus.NOT_FOUND));

        // 저장
        MemeFavorite mf = MemeFavorite.builder()
                .user(user)
                .meme(meme)
                .build();

        memeFavoriteRepository.save(mf);
    }

    @Override
    @Transactional
    public void deleteFavorite(Long userId, Long memeId) {

        if (!memeFavoriteRepository.existsByUser_IdAndMeme_Id(userId, memeId)) {
            throw new CustomException("존재하지 않는 즐겨찾기입니다.",HttpStatus.NOT_FOUND);
        }
        memeFavoriteRepository.deleteByUser_IdAndMeme_Id(userId, memeId);
    }

    @Override
    public MemeResponseDTO getMyFavoriteMemes(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);

        // 페이지 요청
        PageRequest pageable = PageRequest.of(safePage - 1, safeSize);
        Page<MemeFavorite> pageResult = memeFavoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        long total = pageResult.getTotalElements();

        List<MemeItemDTO> items = pageResult.getContent().stream()
                .map(m -> MemeItemDTO.builder()
                        .memeId(m.getMeme().getId())
                        .memeUrl(m.getMeme().getImageUrl())
                        .build())
                .toList();

        return MemeResponseDTO.builder()
                .page(safePage)
                .size(safeSize)
                .total(Math.toIntExact(total))
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MemeResponseDTO getHourlyTop10Memes() {
        List<MemeHourlyTop10> topList = memeHourlyTop10Repository.findLatestTop10();

        if (topList.isEmpty()) {
            // 아직 집계 전이거나 로그 없음
            return MemeResponseDTO.builder()
                    .page(1)
                    .size(0)
                    .total(0)
                    .items(List.of())
                    .build();
        }

        List<MemeItemDTO> items = topList.stream()
                .map(row -> {
                    Meme meme = row.getMeme();
                    return MemeItemDTO.builder()
                            .memeId(meme.getId())
                            .memeUrl(meme.getImageUrl())
                            .build();
                })
                .toList();

        int size = items.size();

        return MemeResponseDTO.builder()
                .page(1)
                .size(size)
                .total(size)
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MemeResponseDTO getTop10MemesByTotalUsage() {
        // usageCnt + downloadCnt 기준 상위 10개
        var memes = memeRepository.findTopByUsageAndDownload(PageRequest.of(0, 10));

        if (memes.isEmpty()) {
            return MemeResponseDTO.builder()
                    .page(1)
                    .size(0)
                    .total(0)
                    .items(List.of())
                    .build();
        }

        List<MemeItemDTO> items = memes.stream()
                .map(meme -> {
                    return MemeItemDTO.builder()
                            .memeId(meme.getId())
                            .memeUrl(meme.getImageUrl())
                            .build();
                })
                .toList();

        int size = items.size();

        return MemeResponseDTO.builder()
                .page(1)
                .size(size)
                .total(size)
                .items(items)
                .build();
    }

    @Override
    public MemeDetailDTO getMemeDetail(Long memeId) {
        Meme meme = memeRepository.findByIdWithCreatorAndTags(memeId)
                .orElseThrow(() -> new CustomException("해당 밈을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        User creator = meme.getCreator();
        MemeCreatorDTO creatorDTO = MemeCreatorDTO.builder()
                .id(creator.getId())
                .userId(creator.getUserId())
                .nickname(creator.getNickname())
                .imgUrl(creator.getImgUrl())
                .build();

        List<String> tags = meme.getMemeTags().stream()
                .map(mt -> mt.getTag().getTagName())
                .distinct()
                .toList();

        int favoriteCnt = (int) memeFavoriteRepository.countByMemeId(memeId);

        return MemeDetailDTO.builder()
                .memeId(meme.getId())
                .imageUrl(meme.getImageUrl())
                .createdAt(meme.getCreatedAt())
                .usageCnt(meme.getUsageCnt())
                .favoriteCnt(favoriteCnt)
                .downloadCnt(meme.getDownloadCnt())
                .creator(creatorDTO)
                .tags(tags)
                .build();
    }

    public MemeResponseDTO searchByTagBasic(String tag, int page, int size) {
        if (tag == null || tag.isBlank()) {
            return MemeResponseDTO.builder()
                    .page(page)
                    .size(size)
                    .total(0)
                    .items(Collections.emptyList())
                    .build();
        }

        // JPQL에서 이미 ORDER BY m.usageCnt DESC 고정
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Meme> resultPage =
                memeRepository.findByTagNameContainingOrderbyUsageCnt(tag.trim(), pageable);

        List<Meme> memes = resultPage.getContent();
        List<Long> memeIds = memes.stream().map(Meme::getId).toList();

        List<MemeItemDTO> items = memes.stream()
                .map(m -> MemeItemDTO.builder()
                        .memeId(m.getId())
                        .memeUrl(m.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return MemeResponseDTO.builder()
                .page(page)
                .size(size)
                .total((int) resultPage.getTotalElements())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public void deleteMeme(Long userId, Long memeId) {
        // 1) 밈 존재 여부 확인
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new CustomException("존재하지 않는 밈입니다.", HttpStatus.NOT_FOUND));

        // 2) 권한 확인 (본인이 생성한 밈인지)
        if (!meme.getCreator().getId().equals(userId)) {
            throw new CustomException("본인이 생성한 밈만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 3) S3 key 추출 (imageUrl은 CDN URL이므로 파싱 필요)
        String imageUrl = meme.getImageUrl();
        String s3Key = extractS3KeyFromCdnUrl(imageUrl);

        // 4) 연관 데이터 삭제
        // MemeTag는 cascade = ALL, orphanRemoval = true 이므로 자동 삭제됨
        // MemeFavorite는 수동 삭제 필요
        memeFavoriteRepository.deleteByMeme_Id(memeId);

        // 5) DB에서 밈 삭제 (MemeTag는 자동 삭제)
        memeRepository.delete(meme);

        // 6) S3에서 파일 삭제
        try {
            memeS3Service.deleteMeme(s3Key);
            log.info("✅ S3 파일 삭제 완료: key={}", s3Key);
        } catch (Exception e) {
            log.error("❌ S3 파일 삭제 실패: key={}, error={}", s3Key, e.getMessage());
            // S3 삭제 실패 시에도 DB는 이미 삭제되었으므로 로그만 남김
        }

        log.info("✅ 밈 삭제 완료: memeId={}, userId={}", memeId, userId);
    }

    @Override
    @Transactional
    public MemeDetailDTO updateMeme(Long userId, Long memeId, MemeUpdateRequestDTO request) {
        // 1) 밈 존재 여부 확인 및 태그와 함께 조회
        Meme meme = memeRepository.findByIdWithCreatorAndTags(memeId)
                .orElseThrow(() -> new CustomException("존재하지 않는 밈입니다.", HttpStatus.NOT_FOUND));

        // 2) 권한 확인 (본인이 생성한 밈인지)
        if (!meme.getCreator().getId().equals(userId)) {
            throw new CustomException("본인이 생성한 밈만 수정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 3) 태그 업데이트 처리 (현재 지원하는 기능)
        if (request.getTags() != null) {
            updateMemeTags(meme, request.getTags());
        }

        // TODO: 향후 다른 속성 업데이트 추가

        // 4) 응답 DTO 생성 (기존 getMemeDetail 로직 재사용)
        return getMemeDetail(memeId);
    }

    /**
     * 밈의 태그를 업데이트하는 내부 메서드
     * @param meme 업데이트할 밈 엔티티
     * @param tags 새로운 태그 리스트
     */
    private void updateMemeTags(Meme meme, List<String> tags) {
        // 1) 태그 정규화 (기존 로직 재사용)
        LinkedHashSet<String> normalizedTags = tags.stream()
                .map(t -> t == null ? "" : t.trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 2) 최소 1개 검증
        if (normalizedTags.isEmpty()) {
            throw new CustomException("태그는 최소 1개 이상 필요합니다.", HttpStatus.BAD_REQUEST);
        }

        // 3) 최대 25개 검증
        if (normalizedTags.size() > 25) {
            throw new CustomException("태그는 최대 25개까지 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        // 4) 기존 태그명 조회
        Set<String> oldTagNames = meme.getMemeTags().stream()
                .map(mt -> mt.getTag().getTagName())
                .collect(Collectors.toSet());

        // 5) 추가할 태그 계산 (newTags - oldTags)
        Set<String> tagsToAdd = normalizedTags.stream()
                .filter(t -> !oldTagNames.contains(t))
                .collect(Collectors.toSet());

        // 6) 삭제할 태그 계산 (oldTags - newTags)
        Set<String> tagsToRemove = oldTagNames.stream()
                .filter(t -> !normalizedTags.contains(t))
                .collect(Collectors.toSet());

        // 7) 태그 삭제 처리
        if (!tagsToRemove.isEmpty()) {
            List<MemeTag> toDelete = meme.getMemeTags().stream()
                    .filter(mt -> tagsToRemove.contains(mt.getTag().getTagName()))
                    .toList();
            
            if (!toDelete.isEmpty()) {
                // DB에서 먼저 삭제
                memeTagRepository.deleteAll(toDelete);
                memeTagRepository.flush();
                
                // 컬렉션에서 제거 (equals/hashCode 기반)
                meme.getMemeTags().removeAll(toDelete);
                
                log.info("🗑️ 밈에서 태그 제거: memeId={}, removedTags={}", meme.getId(), tagsToRemove);
            }
        }

        // 8) 태그 추가 처리
        for (String tagName : tagsToAdd) {
            // 기존 태그 조회 또는 새로 생성
        //     Tag tag = tagRepository.findByTagName(tagName)
        //             .orElseGet(() -> {
        //                 Tag newTag = Tag.builder()
        //                         .tagName(tagName)
        //                         .build();
        //                 return tagRepository.save(newTag);
        //             });

        //     MemeTag mt = MemeTag.of(meme, tag);
        // 수정된 코드
        Tag tag = tagRepository.findByTagName(tagName)
                .orElseGet(() -> {
                    Tag newTag = Tag.builder().tagName(tagName).build();
                    Tag savedTag = tagRepository.save(newTag);
                    tagRepository.flush(); // 강제로 DB에 반영하여 ID 확보
                    return savedTag;
                });

        MemeTag mt = MemeTag.of(meme, tag);
        memeTagRepository.save(mt);
        meme.getMemeTags().add(mt);
        log.info("➕ 밈에 태그 추가: memeId={}, tagName={}", meme.getId(), tagName);
        }

        // TODO: OpenSearch 업데이트 추가 예정
        // try {
        //     String s3Key = extractS3KeyFromCdnUrl(meme.getImageUrl());
        //     ImageDocument imageDocument = ImageDocument.builder()
        //             .s3_url(meme.getImageUrl())
        //             .object_key(s3Key)
        //             .tags(new ArrayList<>(normalizedTags))
        //             .created_at(meme.getCreatedAt())
        //             .build();
        //     searchService.indexImage(imageDocument); // 기존 문서 업데이트
        //     log.info("✅ OpenSearch 업데이트 완료: memeId={}", meme.getId());
        // } catch (Exception e) {
        //     log.error("❌ OpenSearch 업데이트 실패: memeId={}, error={}", meme.getId(), e.getMessage());
        // }

        log.info("✅ 밈 태그 수정 완료: memeId={}, oldTags={}, newTags={}",
                meme.getId(), oldTagNames, normalizedTags);
    }



    // CDN URL에서 S3 key 추출하는 헬퍼 메서드
    private String extractS3KeyFromCdnUrl(String cdnUrl) {
        // CDN URL 예시: https://cdn.example.com/memes%2F2025%2F11%2Fuuid.gif
        // S3 key: memes/2025/11/uuid.gif
        try {
            URI uri = new URI(cdnUrl);
            String path = uri.getPath();
            String decoded = URLDecoder.decode(path, StandardCharsets.UTF_8);
            return decoded.startsWith("/") ? decoded.substring(1) : decoded;
        } catch (Exception e) {
            log.error("CDN URL 파싱 실패: {}", cdnUrl, e);
            throw new RuntimeException("잘못된 이미지 URL입니다.", e);
        }
    }
}
