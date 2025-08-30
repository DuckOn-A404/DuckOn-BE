package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.Subject;
import com.a404.duckonback.entity.SubjectFollow;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.SubjectFollowRepository;
import com.a404.duckonback.repository.SubjectRepository;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SubjectFollowService subjectFollowService;
    private final SubjectFollowRepository subjectFollowRepository;
    private final S3Service s3Service;

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
            .orElseThrow(() -> new CustomException(
                "해당 대상을 찾을 수 없습니다. ID: " + subjectId,
                HttpStatus.NOT_FOUND
            ));

        boolean isFollowed = false;
        LocalDateTime followedAt = null;

        if (userId != null && userRepository.findByIdAndDeletedFalse(userId) != null) {
            Optional<SubjectFollow> followOpt = subjectFollowService.getSubjectFollow(userId, subjectId);
            if (followOpt.isPresent()) {
                isFollowed = true;
                followedAt = followOpt.get().getCreatedAt();
            }
        }

        return SubjectDetailDTO.of(subject, isFollowed, followedAt);
    }

    @Override
    public List<SubjectDTO> searchSubjects(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException("keyword는 필수 파라미터입니다.", HttpStatus.BAD_REQUEST);
        }
        var page = subjectRepository.pageSubjects(
            PageRequest.of(0, 500),
            "name",
            "asc",
            keyword.trim()
        );
        return page.getContent();
    }

    @Override
    public List<SubjectDTO> getRandomSubjects(int size) {
        if (size < 1) throw new CustomException("size는 1 이상의 정수여야 합니다.", HttpStatus.BAD_REQUEST);

        return subjectRepository.findRandomSubjects(size).stream()
            .map(subject -> {
                long cnt = subjectFollowRepository.countBySubject_Id(subject.getId());
                return SubjectDTO.fromEntity(subject, cnt);
            })
            .toList();
    }

    @Override
    public Subject createSubject(AdminSubjectRequestDTO dto) {
        if (subjectRepository.existsByNameEnOrNameKr(dto.getNameEn(), dto.getNameKr())) {
            throw new CustomException("이미 존재하는 대상입니다.", HttpStatus.CONFLICT);
        }

        LocalDate debut = (dto.getDebutDate() != null) ? dto.getDebutDate() : LocalDate.now();

        String imgUrl = null;
        MultipartFile file = dto.getImage();
        if (file != null && !file.isEmpty()) {
            String prefix = String.format("subject/%s", dto.getNameEn());
            imgUrl = s3Service.uploadFile(file, prefix);
        }

        Subject subject = Subject.builder()
            .nameEn(dto.getNameEn())
            .nameKr(dto.getNameKr())
            .debutDate(debut)
            .imgUrl(imgUrl)
            .build();
        return subjectRepository.save(subject);
    }

    @Override
    public Subject updateSubject(Long subjectId, AdminSubjectRequestDTO dto) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다. ID: " + subjectId,
                HttpStatus.NOT_FOUND));

        Optional<Subject> conflict = subjectRepository
            .findByNameEnOrNameKr(dto.getNameEn(), dto.getNameKr())
            .filter(s -> !s.getId().equals(subjectId));
        if (conflict.isPresent()) throw new CustomException("이미 존재하는 대상 이름입니다.", HttpStatus.CONFLICT);

        LocalDate debut = (dto.getDebutDate() != null) ? dto.getDebutDate() : subject.getDebutDate();
        subject.setDebutDate(debut);
        subject.setNameEn(dto.getNameEn());
        subject.setNameKr(dto.getNameKr());

        MultipartFile file = dto.getImage();
        if (file != null && !file.isEmpty()) {
            if (subject.getImgUrl() != null) s3Service.deleteFile(subject.getImgUrl());
            String prefix = String.format("subject/%s", dto.getNameEn());
            String newUrl = s3Service.uploadFile(file, prefix);
            subject.setImgUrl(newUrl);
        }

        return subjectRepository.save(subject);
    }

    @Override
    public Subject patchSubject(Long subjectId, AdminSubjectPatchDTO dto) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다. ID: " + subjectId,
                HttpStatus.NOT_FOUND));

        if (dto.getNameEn() != null || dto.getNameKr() != null) {
            Optional<Subject> conflict = subjectRepository.findByNameEnOrNameKr(
                dto.getNameEn() != null ? dto.getNameEn() : subject.getNameEn(),
                dto.getNameKr() != null ? dto.getNameKr() : subject.getNameKr()
            ).filter(s -> !s.getId().equals(subjectId));
            if (conflict.isPresent()) {
                throw new CustomException("이미 존재하는 대상 이름입니다.", HttpStatus.CONFLICT);
            }
        }

        if (dto.getDebutDate() != null) subject.setDebutDate(dto.getDebutDate());
        if (dto.getNameEn() != null)   subject.setNameEn(dto.getNameEn());
        if (dto.getNameKr() != null)   subject.setNameKr(dto.getNameKr());

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            if (subject.getImgUrl() != null) s3Service.deleteFile(subject.getImgUrl());
            String prefix = String.format("subject/%s", subject.getNameEn());
            String newUrl = s3Service.uploadFile(dto.getImage(), prefix);
            subject.setImgUrl(newUrl);
        }

        return subjectRepository.save(subject);
    }

    @Override
    public String findSlugById(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다. ID: " + subjectId,
                HttpStatus.NOT_FOUND));

        try {
            var slugField = Subject.class.getDeclaredField("slug");
            slugField.setAccessible(true);
            Object val = slugField.get(subject);
            if (val instanceof String s && !s.isBlank()) return s;
        } catch (NoSuchFieldException | IllegalAccessException ignore) {}

        String base = subject.getNameEn() != null ? subject.getNameEn()
            : subject.getNameKr() != null ? subject.getNameKr()
                : String.valueOf(subjectId);
        return slugify(base);
    }

    private String slugify(String s) {
        String normalized = s.trim()
            .replaceAll("[^0-9A-Za-z가-힣\\-\\s]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-|-$", "");
        return normalized;
    }

    @Override
    public Page<SubjectDTO> getSubjects(Pageable pageable, String sort, String order, String keyword) {
        String sortKey = (sort == null) ? "followers" : sort.toLowerCase();
        if (!List.of("followers", "name", "debut").contains(sortKey)) sortKey = "followers";
        String sortOrder = (order == null) ? "desc" : order.toLowerCase();
        if (!List.of("asc", "desc").contains(sortOrder)) sortOrder = "desc";
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        return subjectRepository.pageSubjects(pageable, sortKey, sortOrder, kw);
    }

    @Override
    public Page<SubjectDTO> getSubjects(Pageable pageable) {
        return getSubjects(pageable, "followers", "desc", null);
    }
}
