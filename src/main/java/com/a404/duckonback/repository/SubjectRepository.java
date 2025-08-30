package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Subject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long>, SubjectRepositoryCustom {

    @Query("SELECT sf.subject.id FROM SubjectFollow sf WHERE sf.user.id = :id")
    List<Long> findAllSubjectIdByUserId(@Param("id") Long id);

    // size 만큼 랜덤 Subject
    @Query(value = "SELECT * FROM subject ORDER BY RAND() LIMIT :size", nativeQuery = true)
    List<Subject> findRandomSubjects(@Param("size") int size);

    Optional<Subject> findBySlug(String slug);
}
