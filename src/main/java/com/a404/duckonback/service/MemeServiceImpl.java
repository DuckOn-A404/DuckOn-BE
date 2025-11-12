package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.*;
import com.a404.duckonback.entity.Meme;
import com.a404.duckonback.entity.MemeTag;
import com.a404.duckonback.entity.Tag;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.dto.MemeCreateResponseDTO.MemeInfoDTO;
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
                    // 태그 목록
                    List<String> tags = java.util.Optional.ofNullable(meme.getMemeTags())
                            .orElse(java.util.Collections.emptySet())
                            .stream()
                            .map(mt -> mt.getTag() != null ? mt.getTag().getTagName() : null)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .toList();

                    return MemeItemDTO.builder()
                            .memeId(meme.getId())
                            .memeUrl(meme.getImageUrl())
                            .tags(tags)
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
    public List<FavoriteMemeDTO> getMyFavoriteMemes(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);

        // 페이지 요청
        PageRequest pageable = PageRequest.of(safePage - 1, safeSize);
        Page<MemeFavorite> favPage = memeFavoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);


        return favPage.getContent().stream()
                .map(mf -> {
                    var meme = mf.getMeme();
                    var tags = java.util.Optional.ofNullable(meme.getMemeTags())
                            .orElse(java.util.Collections.emptySet())
                            .stream()
                            .map(mt -> mt.getTag() != null ? mt.getTag().getTagName() : null)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .toList();

                    return FavoriteMemeDTO.builder()
                            .memeId(meme.getId())
                            .memeUrl(meme.getImageUrl())
                            .tags(tags)
                            .favoritedAt(mf.getCreatedAt())
                            .build();
                })
                .toList();
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

                    List<String> tags = Optional.ofNullable(meme.getMemeTags())
                            .orElseGet(Set::of)
                            .stream()
                            .map(mt -> mt.getTag() != null ? mt.getTag().getTagName() : null)
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();

                    return MemeItemDTO.builder()
                            .memeId(meme.getId())
                            .memeUrl(meme.getImageUrl())
                            .tags(tags)
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
                    List<String> tags = Optional.ofNullable(meme.getMemeTags())
                            .orElseGet(Collections::emptySet)
                            .stream()
                            .map(mt -> mt.getTag() != null ? mt.getTag().getTagName() : null)
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();

                    return MemeItemDTO.builder()
                            .memeId(meme.getId())
                            .memeUrl(meme.getImageUrl())
                            .tags(tags)
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

    @Override
    public List<MyMemeDTO> getMyMemes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        return memeRepository
                .findMyMemesByCreatorIdOrderByCreatedAtDesc(userId, pageable)
                .getContent();
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

        // 밈별 태그 일괄 조회 (N+1 방지)
        Map<Long, List<String>> tagsByMemeId = new HashMap<>();
        if (!memeIds.isEmpty()) {
            for (Object[] row : memeTagRepository.findTagPairsByMemeIds(memeIds)) {
                Long memeId = (Long) row[0];
                String tagName = (String) row[1];
                tagsByMemeId.computeIfAbsent(memeId, k -> new ArrayList<>()).add(tagName);
            }
        }

        List<MemeItemDTO> items = memes.stream()
                .map(m -> MemeItemDTO.builder()
                        .memeId(m.getId())
                        .memeUrl(m.getImageUrl())
                        .tags(tagsByMemeId.getOrDefault(m.getId(), List.of()))
                        .build())
                .collect(Collectors.toList());

        return MemeResponseDTO.builder()
                .page(page)
                .size(size)
                .total((int) resultPage.getTotalElements())
                .items(items)
                .build();
    }
}
