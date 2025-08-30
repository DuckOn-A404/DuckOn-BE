package com.a404.duckonback.service;

import com.a404.duckonback.entity.Subject;
import com.a404.duckonback.entity.SubjectFollow;
import com.a404.duckonback.entity.SubjectFollowId;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

public interface SubjectFollowService {

    SubjectFollow createSubjectFollow(SubjectFollow subjectFollow);
    Optional<SubjectFollow> getSubjectFollow(Long userId, Long subjectId);

    List<SubjectFollow> getFollowsByUser(Long id);
    List<SubjectFollow> getFollowsBySubject(Long subjectId);

    void deleteSubjectFollow(Long userId, Long subjectId);
    boolean isFollowingSubject(Long userId, Long subjectId);
    void followSubjects(Long userId, List<Long> subjectList);

    Page<FollowedSubjectDTO> getFollowedSubjects(Long userId, Pageable pageable);
    void followSubject(Long userId, Long subjectId);
    void unfollowSubject(Long userId, Long subjectId);
    void updateSubjectFollows(Long userId, List<Long> subjectIds);
}
