package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Subject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubjectRepository extends JpaRepository<Subject, Long>, SubjectRepositoryCustom {

    @Query("SELECT sf.subject.id FROM SubjectFollow sf WHERE sf.user.id = :id")
    List<Long> findAllSubjectIdByUserId(@Param("id") Long id);

    @Query(value = "SELECT * FROM subject ORDER BY RAND() LIMIT :size", nativeQuery = true)
    List<Subject> findRandomSubjects(@Param("size") int size);

    boolean existsBySlug(String slug);

    @Query("""
        select distinct s from Subject s
        left join fetch s.domain d
        left join fetch s.primaryCategory pc
        left join fetch s.categories c
        where s.id = :id
        """)
    Optional<Subject> findDetailWithTaxonomy(@Param("id") Long id);

    // === 카테고리 필터(ANY) ===
    @Query("""
        select distinct s from Subject s
        join s.domain d
        left join s.categories c
        where (:domainCode is null or d.code = :domainCode)
          and (:codesEmpty = true or c.code in :codes)
        """)
    Page<Subject> findByDomainAndAnyCategory(
        @Param("domainCode") String domainCode,
        @Param("codes") List<String> codes,
        @Param("codesEmpty") boolean codesEmpty,
        Pageable pageable);

    // === 카테고리 필터(ALL) ===
    @Query("""
        select s from Subject s
        join s.domain d
        join s.categories c
        where (:domainCode is null or d.code = :domainCode)
          and c.code in :codes
        group by s
        having count(distinct c.code) = :codesSize
        """)
    Page<Subject> findByDomainAndAllCategories(
        @Param("domainCode") String domainCode,
        @Param("codes") List<String> codes,
        @Param("codesSize") long codesSize,
        Pageable pageable);
}
