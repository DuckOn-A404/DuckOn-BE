package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long>, SubjectRepositoryCustom {

    @Query("SELECT sf.subject.id FROM SubjectFollow sf WHERE sf.user.id = :id")
    List<Long> findAllSubjectIdByUserId(@Param("id") Long id);

    @Query("""
        SELECT s FROM Subject s
        WHERE LOWER(s.nameKr) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    List<Subject> searchByKeyword(@Param("keyword") String keyword);

    // size 만큼 랜덤 Subject
    @Query(value = "SELECT * FROM subject ORDER BY RAND() LIMIT :size", nativeQuery = true)
    List<Subject> findRandomSubjects(@Param("size") int size);

    // JpaRepository 기본 제공: Optional<Subject> findById(Long id)
    boolean existsByNameEnOrNameKr(String nameEn, String nameKr);
}
