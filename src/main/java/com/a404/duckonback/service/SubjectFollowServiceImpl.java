package com.a404.duckonback.service;

import com.a404.duckonback.dto.FollowedSubjectDTO;
import com.a404.duckonback.entity.Subject;
import com.a404.duckonback.entity.SubjectFollow;
import com.a404.duckonback.entity.SubjectFollowId;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.SubjectFollowRepository;
import com.a404.duckonback.repository.SubjectRepository;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.util.SubjectDisplayNameResolver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectFollowServiceImpl implements SubjectFollowService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SubjectFollowRepository subjectFollowRepository;
    private final SubjectDisplayNameResolver displayNameResolver;

    private static final String DEFAULT_DISPLAY_LOCALE = "ko";

    @Override
    public SubjectFollow createSubjectFollow(SubjectFollow subjectFollow) {
        return subjectFollowRepository.save(subjectFollow);
    }

    @Override
    public Optional<SubjectFollow> getSubjectFollow(Long userId, Long subjectId) {
        return subjectFollowRepository.findById(new SubjectFollowId(subjectId, userId));
    }

    @Override
    public List<SubjectFollow> getFollowsByUser(Long id) {
        return subjectFollowRepository.findByUser_Id(id);
    }

    @Override
    public List<SubjectFollow> getFollowsBySubject(Long subjectId) {
        return subjectFollowRepository.findBySubject_Id(subjectId);
    }

    @Override
    public void deleteSubjectFollow(Long userId, Long subjectId) {
        subjectFollowRepository.deleteByUser_IdAndSubject_Id(userId, subjectId);
    }

    @Override
    public boolean isFollowingSubject(Long userId, Long subjectId) {
        return subjectFollowRepository.existsByUser_IdAndSubject_Id(userId, subjectId);
    }

    @Override
    public void followSubjects(Long userId, List<Long> subjectList){
        User user = userRepository.findByIdAndDeletedFalse(userId);

        for (Long subjectId : subjectList) {
            Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new CustomException("존재하지 않는 대상입니다. ID: " + subjectId, HttpStatus.NOT_FOUND));

            SubjectFollow follow = SubjectFollow.builder()
                .user(user)
                .subject(subject)
                .createdAt(LocalDateTime.now())
                .build();

            subjectFollowRepository.save(follow);
        }
    }

    @Override
    public Page<FollowedSubjectDTO> getFollowedSubjects(Long userId, Pageable pageable) {
        var user = userRepository.findByIdAndDeletedFalse(userId);
        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);
        }
        String preferredLocale = (user.getLanguage() == null || user.getLanguage().isBlank())
            ? DEFAULT_DISPLAY_LOCALE : user.getLanguage();

        return subjectFollowRepository.findByUser_Id(userId, pageable)
            .map(sf -> {
                Subject s = sf.getSubject();
                String displayName = displayNameResolver.resolve(s, preferredLocale);
                return FollowedSubjectDTO.builder()
                    .subjectId(s.getId())
                    .slug(s.getSlug())
                    .displayName(displayName)
                    .debutDate(s.getDebutDate())
                    .imgUrl(s.getImgUrl())
                    .build();
            });
    }

    @Override
    public void followSubject(Long userId, Long subjectId) {
        User user = userRepository.findByIdAndDeletedFalse(userId);
        if (user == null) throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);

        if (subjectFollowRepository.existsByUser_IdAndSubject_Id(userId, subjectId)) {
            throw new CustomException("이미 팔로우한 대상입니다.", HttpStatus.BAD_REQUEST);
        }

        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        SubjectFollow sf = SubjectFollow.builder()
            .user(user)
            .subject(subject)
            .createdAt(LocalDateTime.now())
            .build();
        subjectFollowRepository.save(sf);
    }

    @Override
    @Transactional
    public void unfollowSubject(Long userId, Long subjectId) {
        User user = userRepository.findByIdAndDeletedFalse(userId);
        if (user == null) throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);

        subjectRepository.findById(subjectId)
            .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!subjectFollowRepository.existsByUser_IdAndSubject_Id(userId, subjectId)) {
            throw new CustomException("팔로우한 대상이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        subjectFollowRepository.deleteByUser_IdAndSubject_Id(userId, subjectId);
    }

    @Override
    @Transactional
    public void updateSubjectFollows(Long userId, List<Long> subjectIds) {
        User user = userRepository.findByIdAndDeletedFalse(userId);
        if (user == null) throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);

        List<Long> notFound = subjectIds.stream()
            .filter(id -> subjectRepository.findById(id).isEmpty())
            .toList();
        if (!notFound.isEmpty()) {
            throw new CustomException("다음 대상을 찾을 수 없습니다: " + notFound, HttpStatus.NOT_FOUND);
        }

        Set<Long> existing = subjectFollowRepository.findByUser_Id(userId).stream()
            .map(sf -> sf.getSubject().getId())
            .collect(Collectors.toSet());

        Set<Long> requested = Set.copyOf(subjectIds);

        Set<Long> toFollow = requested.stream().filter(id -> !existing.contains(id)).collect(Collectors.toSet());
        Set<Long> toUnfollow = existing.stream().filter(id -> !requested.contains(id)).collect(Collectors.toSet());

        for (Long sid : toFollow) {
            Subject subject = subjectRepository.findById(sid)
                .orElseThrow(() -> new CustomException("대상을 찾을 수 없습니다: " + sid, HttpStatus.NOT_FOUND));
            subjectFollowRepository.save(
                SubjectFollow.builder()
                    .user(user)
                    .subject(subject)
                    .createdAt(LocalDateTime.now())
                    .build()
            );
        }

        for (Long sid : toUnfollow) {
            subjectFollowRepository.deleteByUser_IdAndSubject_Id(userId, sid);
        }
    }
}
