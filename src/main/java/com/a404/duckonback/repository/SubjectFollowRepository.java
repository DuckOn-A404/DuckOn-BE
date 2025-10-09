package com.a404.duckonback.repository;

import com.a404.duckonback.entity.SubjectFollow;
import com.a404.duckonback.entity.SubjectFollowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectFollowRepository extends JpaRepository<SubjectFollow, SubjectFollowId> {
    List<SubjectFollow> findByUser_Id(Long id);
    List<SubjectFollow> findBySubject_Id(Long subjectId);

    boolean existsByUser_IdAndSubject_Id(Long userId, Long subjectId);
    void deleteByUser_IdAndSubject_Id(Long userId, Long subjectId);

    Optional<SubjectFollow> findByUser_IdAndSubject_Id(Long userId, Long subjectId);

    // 특정 Subject 팔로워 수
    long countBySubject_Id(Long subjectId);

    // 페이징
    Page<SubjectFollow> findByUser_Id(Long userId, Pageable pageable);
}
