package com.a404.duckonback.repository;

import com.a404.duckonback.entity.SubjectName;
import com.a404.duckonback.enums.SubjectNameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubjectNameRepository extends JpaRepository<SubjectName, Long> {

    List<SubjectName> findBySubject_Id(Long subjectId);

    // DDL의 UNIQUE(subject_id, locale_tag, name)와 일치하게 중복검사
    boolean existsBySubject_IdAndLocaleTagAndName(Long subjectId, String localeTag, String name);

    // 우선순위/대표 여부를 고려해 1건 픽 (derived query로도 가능)
    // 특정 locale의 OFFICIAL 대표명 1건
    Optional<SubjectName>
    findFirstBySubject_IdAndLocaleTagAndNameTypeOrderByPrimaryDescPriorityDesc(
        Long subjectId, String localeTag, SubjectNameType nameType);

    // 특정 타입(OFFICIAL/ROMANIZED/TRANSLATED/ALIAS)의 대표명 1건
    Optional<SubjectName>
    findFirstBySubject_IdAndNameTypeOrderByPrimaryDescPriorityDesc(
        Long subjectId, SubjectNameType nameType);

    // 리졸버용 헬퍼 (원하면 @Query로 한 번에 문자열만 뽑아내도 됨)
    @Query("""
        select sn from SubjectName sn
        where sn.subject.id = :subjectId and sn.nameType = :type
        order by sn.primary desc, sn.priority asc
    """)
    List<SubjectName> pickAllByType(Long subjectId, SubjectNameType type);

    // 리졸버의 편의 메서드(문자열 바로 받기 원할 때)
    default Optional<String> pickOfficialName(Long subjectId, String locale) {
        return findFirstBySubject_IdAndLocaleTagAndNameTypeOrderByPrimaryDescPriorityDesc(
            subjectId, locale, SubjectNameType.OFFICIAL
        ).map(SubjectName::getName);
    }

    default Optional<String> pickAnyByType(Long subjectId, SubjectNameType type) {
        return findFirstBySubject_IdAndNameTypeOrderByPrimaryDescPriorityDesc(
            subjectId, type
        ).map(SubjectName::getName);
    }
}