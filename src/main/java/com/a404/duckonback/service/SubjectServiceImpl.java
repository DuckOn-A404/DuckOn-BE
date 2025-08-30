package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.*;
import com.a404.duckonback.enums.SubjectNameType;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectFollowRepository subjectFollowRepository;
    private final SubjectNameRepository subjectNameRepository;
    private final DomainRepository domainRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    // 기본 표시 로케일(사용자 선호 없을 때)
    private static final String DEFAULT_DISPLAY_LOCALE = "ko";

    // --------------------------
    // 조회
    // --------------------------
    @Override
    public Subject findById(Long subjectId) {
        return subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<Long> findAllSubjectIdByUserId(Long id){
        return subjectRepository.findAllSubjectIdByUserId(id);
    }

    @Override
    public SubjectDetailDTO getSubjectDetail(Long userId, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("해당 대상을 찾을 수 없습니다. ID: " + subjectId, HttpStatus.NOT_FOUND));

        boolean isFollowed = false;
        LocalDateTime followedAt = null;
        if (userId != null && userRepository.findByIdAndDeletedFalse(userId) != null) {
            Optional<SubjectFollow> followOpt = subjectFollowRepository.findByUser_IdAndSubject_Id(userId, subjectId);
            if (followOpt.isPresent()) {
                isFollowed = true;
                followedAt = followOpt.get().getCreatedAt();
            }
        }

        String displayName = resolveDisplayName(subject, DEFAULT_DISPLAY_LOCALE);
        return SubjectDetailDTO.of(subject, displayName, isFollowed, followedAt);
    }

    @Override
    public List<SubjectDTO> searchSubjects(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new CustomException("keyword는 필수 파라미터입니다.", HttpStatus.BAD_REQUEST);
        }
        var page = subjectRepository.pageSubjects(
            PageRequest.of(0, 500),
            "name",
            "asc",
            keyword.trim(),
            DEFAULT_DISPLAY_LOCALE
        );
        return page.getContent();
    }

    @Override
    public List<SubjectDTO> getRandomSubjects(int size) {
        if (size < 1) throw new CustomException("size는 1 이상의 정수여야 합니다.", HttpStatus.BAD_REQUEST);

        return subjectRepository.findRandomSubjects(size).stream()
            .map(s -> {
                long cnt = subjectFollowRepository.countBySubject_Id(s.getId());
                return SubjectDTO.builder()
                    .subjectId(s.getId())
                    .slug(s.getSlug())
                    .displayName(resolveDisplayName(s, DEFAULT_DISPLAY_LOCALE))
                    .debutDate(s.getDebutDate())
                    .imgUrl(s.getImgUrl())
                    .followerCount(cnt)
                    .build();
            })
            .toList();
    }

    // --------------------------
    // 생성
    // --------------------------
    @Override
    public Subject createSubject(AdminSubjectRequestDTO dto) {
        // 필수값 검증
        if (dto.getDomainId() == null) {
            throw new CustomException("domainId는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (dto.getNativeLocale() == null || dto.getNativeLocale().isBlank()) {
            throw new CustomException("nativeLocale은 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (dto.getCountryCode() == null || dto.getCountryCode().isBlank()) {
            throw new CustomException("countryCode는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (dto.getEnglishName() == null || dto.getEnglishName().isBlank()) {
            // 불변 slug 생성 위해 영어 이름 필수(정책)
            throw new CustomException("영문 이름(englishName)은 필수입니다.", HttpStatus.BAD_REQUEST);
        }

        Domain domain = domainRepository.findById(dto.getDomainId())
            .orElseThrow(() -> new CustomException("존재하지 않는 도메인입니다.", HttpStatus.NOT_FOUND));
        Category primaryCategory = null;
        if (dto.getPrimaryCategoryId() != null) {
            primaryCategory = categoryRepository.findById(dto.getPrimaryCategoryId())
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND));
        }

        // 슬러그 결정
        final String provided = (dto.getSlug() == null ? null : dto.getSlug().trim());
        final String slugCandidate = (provided != null && !provided.isBlank())
            ? slugify(provided)
            : slugify(dto.getEnglishName()); // 입력 없으면 영어이름 기반

        if (slugCandidate.isBlank()) {
            throw new CustomException("슬러그를 생성할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        final String finalSlug;
        if (provided != null && !provided.isBlank()) {
            // 직접 입력한 슬러그는 중복이면 에러(의도한 주소를 보장)
            if (subjectRepository.existsBySlug(slugCandidate)) {
                throw new CustomException("이미 사용 중인 슬러그입니다.", HttpStatus.CONFLICT);
            }
            finalSlug = slugCandidate;
        } else {
            // 자동 생성된 경우에는 suffix로 유니크 보장
            finalSlug = ensureUniqueSlug(slugCandidate);
        }

        // 이미지 업로드(선택)
        String imgUrl = null;
        MultipartFile file = dto.getImage();
        if (file != null && !file.isEmpty()) {
            String prefix = "subject/" + finalSlug;
            imgUrl = s3Service.uploadFile(file, prefix);
        }

        LocalDate debut = (dto.getDebutDate() != null) ? dto.getDebutDate() : null;

        Subject subject = Subject.builder()
            .domain(domain)
            .primaryCategory(primaryCategory)
            .nativeLocale(dto.getNativeLocale().toLowerCase())
            .countryCode(dto.getCountryCode().toUpperCase())
            .slug(finalSlug)
            .debutDate(debut)
            .imgUrl(imgUrl)
            .build();

        // 연관 편의
        subject = subjectRepository.save(subject);

        // 다국어 이름 저장
        upsertOfficialName(subject, "en", dto.getEnglishName());
        if (dto.getKoreanName() != null && !dto.getKoreanName().isBlank()) {
            upsertOfficialName(subject, "ko", dto.getKoreanName());
        }
        if (dto.getNativeName() != null && !dto.getNativeName().isBlank()) {
            upsertOfficialName(subject, dto.getNativeLocale().toLowerCase(), dto.getNativeName());
        }

        return subject;
    }

    // --------------------------
    // 수정 (PUT/전체)
    // --------------------------
    @Override
    public Subject updateSubject(Long subjectId, AdminSubjectRequestDTO dto) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다. ID: " + subjectId, HttpStatus.NOT_FOUND));

        // slug는 불변(정책)
        if (dto.getDomainId() != null) {
            Domain domain = domainRepository.findById(dto.getDomainId())
                .orElseThrow(() -> new CustomException("존재하지 않는 도메인입니다.", HttpStatus.NOT_FOUND));
            subject.setDomain(domain);
        }
        if (dto.getPrimaryCategoryId() != null) {
            Category cat = categoryRepository.findById(dto.getPrimaryCategoryId())
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND));
            subject.setPrimaryCategory(cat);
        }
        if (dto.getNativeLocale() != null && !dto.getNativeLocale().isBlank()) {
            subject.setNativeLocale(dto.getNativeLocale().toLowerCase());
        }
        if (dto.getCountryCode() != null && !dto.getCountryCode().isBlank()) {
            subject.setCountryCode(dto.getCountryCode().toUpperCase());
        }
        subject.setDebutDate(dto.getDebutDate());

        // 이미지 교체
        MultipartFile file = dto.getImage();
        if (file != null && !file.isEmpty()) {
            if (subject.getImgUrl() != null) s3Service.deleteFile(subject.getImgUrl());
            String newUrl = s3Service.uploadFile(file, "subject/" + subject.getSlug());
            subject.setImgUrl(newUrl);
        }

        subject = subjectRepository.save(subject);

        // 이름 변경(있을 경우만 upsert)
        if (dto.getEnglishName() != null && !dto.getEnglishName().isBlank()) {
            upsertOfficialName(subject, "en", dto.getEnglishName());
        }
        if (dto.getKoreanName() != null && !dto.getKoreanName().isBlank()) {
            upsertOfficialName(subject, "ko", dto.getKoreanName());
        }
        if (dto.getNativeName() != null && !dto.getNativeName().isBlank()) {
            String loc = (dto.getNativeLocale() != null && !dto.getNativeLocale().isBlank())
                ? dto.getNativeLocale().toLowerCase()
                : subject.getNativeLocale();
            upsertOfficialName(subject, loc, dto.getNativeName());
        }

        return subject;
    }

    // --------------------------
    // 부분 수정 (PATCH)
    // --------------------------
    @Override
    public Subject patchSubject(Long subjectId, AdminSubjectPatchDTO dto) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다. ID: " + subjectId, HttpStatus.NOT_FOUND));

        if (dto.getDomainId() != null) {
            Domain domain = domainRepository.findById(dto.getDomainId())
                .orElseThrow(() -> new CustomException("존재하지 않는 도메인입니다.", HttpStatus.NOT_FOUND));
            subject.setDomain(domain);
        }
        if (dto.getPrimaryCategoryId() != null) {
            Category cat = categoryRepository.findById(dto.getPrimaryCategoryId())
                .orElseThrow(() -> new CustomException("존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND));
            subject.setPrimaryCategory(cat);
        }
        if (dto.getNativeLocale() != null && !dto.getNativeLocale().isBlank()) {
            subject.setNativeLocale(dto.getNativeLocale().toLowerCase());
        }
        if (dto.getCountryCode() != null && !dto.getCountryCode().isBlank()) {
            subject.setCountryCode(dto.getCountryCode().toUpperCase());
        }
        if (dto.getDebutDate() != null) subject.setDebutDate(dto.getDebutDate());

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            if (subject.getImgUrl() != null) s3Service.deleteFile(subject.getImgUrl());
            String newUrl = s3Service.uploadFile(dto.getImage(), "subject/" + subject.getSlug());
            subject.setImgUrl(newUrl);
        }

        subject = subjectRepository.save(subject);

        // 이름 upsert(있을 때만)
        if (dto.getEnglishName() != null && !dto.getEnglishName().isBlank()) {
            upsertOfficialName(subject, "en", dto.getEnglishName());
        }
        if (dto.getKoreanName() != null && !dto.getKoreanName().isBlank()) {
            upsertOfficialName(subject, "ko", dto.getKoreanName());
        }
        if (dto.getNativeName() != null && !dto.getNativeName().isBlank()) {
            String loc = (dto.getNativeLocale() != null && !dto.getNativeLocale().isBlank())
                ? dto.getNativeLocale().toLowerCase()
                : subject.getNativeLocale();
            upsertOfficialName(subject, loc, dto.getNativeName());
        }

        return subject;
    }

    // --------------------------
    // slug 조회
    // --------------------------
    @Override
    public String findSlugById(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다. ID: " + subjectId, HttpStatus.NOT_FOUND));
        return subject.getSlug(); // 불변 정책
    }

    // --------------------------
    // 목록 (페이지네이션)
    // --------------------------
    @Override
    public Page<SubjectDTO> getSubjects(Pageable pageable, String sort, String order, String keyword) {
        // 표시 로케일: 현재는 고정, 필요하면 파라미터/컨텍스트로 주입
        String disp = DEFAULT_DISPLAY_LOCALE;

        String sortKey = (sort == null) ? "followers" : sort.toLowerCase();
        if (!List.of("followers", "name", "debut").contains(sortKey)) sortKey = "followers";

        String sortOrder = (order == null) ? "desc" : order.toLowerCase();
        if (!List.of("asc", "desc").contains(sortOrder)) sortOrder = "desc";

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        return subjectRepository.pageSubjects(pageable, sortKey, sortOrder, kw, disp);
    }

    @Override
    public Page<SubjectDTO> getSubjects(Pageable pageable) {
        return getSubjects(pageable, "followers", "desc", null);
    }

    @Override
    public String slugify(String s) {
        if (s == null) return "subject";
        // 유니코드 분해 → ASCII만 남김 → 소문자/하이픈 규칙
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKD)
            .replaceAll("[^\\p{ASCII}]", "");
        n = n.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")   // 영숫자/공백/하이픈만
            .replaceAll("\\s+", "-")           // 공백 → 하이픈
            .replaceAll("-{2,}", "-")          // 연속 하이픈 정리
            .replaceAll("^-|-$", "");          // 앞뒤 하이픈 제거
        return n.isBlank() ? "subject" : n;
    }

    // =====================================================================
    // 헬퍼들
    // =====================================================================

    /** 표시명 선택: displayLocale OFFICIAL → native OFFICIAL → en OFFICIAL → 아무 OFFICIAL */
    private String resolveDisplayName(Subject s, String displayLocale) {
        List<SubjectName> names = subjectNameRepository.findBySubject_Id(s.getId());
        if (names.isEmpty()) return s.getSlug(); // 최후의 보루

        String disp = (displayLocale == null || displayLocale.isBlank())
            ? DEFAULT_DISPLAY_LOCALE
            : displayLocale.toLowerCase();

        // 1) displayLocale + OFFICIAL
        Optional<SubjectName> n1 = names.stream()
            .filter(n -> "OFFICIAL".equals(n.getNameType().name()) && disp.equalsIgnoreCase(n.getLocaleTag()))
            .sorted(Comparator.comparingInt(SubjectName::getPriority).reversed())
            .findFirst();
        if (n1.isPresent()) return n1.get().getName();

        // 2) nativeLocale + OFFICIAL
        Optional<SubjectName> n2 = names.stream()
            .filter(n -> "OFFICIAL".equals(n.getNameType().name()) && s.getNativeLocale().equalsIgnoreCase(n.getLocaleTag()))
            .sorted(Comparator.comparingInt(SubjectName::getPriority).reversed())
            .findFirst();
        if (n2.isPresent()) return n2.get().getName();

        // 3) en + OFFICIAL
        Optional<SubjectName> n3 = names.stream()
            .filter(n -> "OFFICIAL".equals(n.getNameType().name()) && "en".equalsIgnoreCase(n.getLocaleTag()))
            .sorted(Comparator.comparingInt(SubjectName::getPriority).reversed())
            .findFirst();
        if (n3.isPresent()) return n3.get().getName();

        // 4) 아무 OFFICIAL
        Optional<SubjectName> n4 = names.stream()
            .filter(n -> "OFFICIAL".equals(n.getNameType().name()))
            .sorted(Comparator.comparingInt(SubjectName::getPriority).reversed())
            .findFirst();
        return n4.map(SubjectName::getName).orElseGet(s::getSlug);
    }

//    /** 영어 이름으로 slug 생성 (ASCII, 소문자, 하이픈) */
//    private String slugifyAscii(String input) {
//        String n = Normalizer.normalize(input, Normalizer.Form.NFKD)
//            .replaceAll("[^\\p{ASCII}]", ""); // 비 ASCII 제거
//        n = n.toLowerCase()
//            .replaceAll("[^a-z0-9\\s-]", "") // 영숫자/공백/하이픈 외 제거
//            .replaceAll("\\s+", "-")        // 공백 → 하이픈
//            .replaceAll("-{2,}", "-")       // 연속 하이픈 정리
//            .replaceAll("^-|-$", "");       // 양끝 하이픈 제거
//        if (n.isBlank()) n = "subject";
//        return n;
//    }

    /** uk_subject_slug 충돌 피해서 숫자 suffix 붙여 유니크 보장 */
    private String ensureUniqueSlug(String base) {
        String slug = base;
        AtomicInteger seq = new AtomicInteger(1);
        while (subjectRepository.existsBySlug(slug)) {
            slug = base + "-" + seq.getAndIncrement();
        }
        return slug;
    }

    /** OFFICIAL 이름 upsert: 동일 locale의 기존 isPrimary를 false로 내리고 새 이름을 대표로 등록 */
    private void upsertOfficialName(Subject subject, String locale, String name) {
        if (name == null || name.isBlank()) return;
        String loc = locale.toLowerCase();

        // 동일 이름이 이미 있으면 대표만 갱신(우선순위 올림)
        List<SubjectName> existing = subjectNameRepository.findBySubject_Id(subject.getId());
        boolean sameExists = existing.stream().anyMatch(n ->
            n.getLocaleTag().equalsIgnoreCase(loc)
                && n.getName().equals(name)
                && n.getNameType() == SubjectNameType.OFFICIAL
        );

        // 동일 locale의 기존 대표 해제
        existing.stream()
            .filter(n -> n.getLocaleTag().equalsIgnoreCase(loc) && n.getNameType() == SubjectNameType.OFFICIAL && n.isPrimary())
            .forEach(n -> { n.setPrimary(false); subjectNameRepository.save(n); });

        if (sameExists) {
            // 같은 이름이 있으면 그걸 대표로 승격
            existing.stream()
                .filter(n -> n.getLocaleTag().equalsIgnoreCase(loc)
                    && n.getName().equals(name)
                    && n.getNameType() == SubjectNameType.OFFICIAL)
                .findFirst()
                .ifPresent(n -> {
                    n.setPrimary(true);
                    n.setPriority((short) (n.getPriority() + 1));
                    subjectNameRepository.save(n);
                });
        } else {
            SubjectName toSave = SubjectName.builder()
                .subject(subject)
                .localeTag(loc)
                .name(name)
                .nameType(SubjectNameType.OFFICIAL)
                .primary(true)
                .priority((short) 100) // 기본 가중치
                .build();
            subjectNameRepository.save(toSave);
        }
    }
}
