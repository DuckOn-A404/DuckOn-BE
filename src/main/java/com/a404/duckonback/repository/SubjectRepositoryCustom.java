package com.a404.duckonback.repository;

import com.a404.duckonback.dto.SubjectDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubjectRepositoryCustom {
    /**
     * keyword(옵션), sort(followers|name|debut), order(asc|desc)를 적용하여
     * N+1 없이 페이지 단위로 SubjectDTO를 반환합니다.
     */
    Page<SubjectDTO> pageSubjects(Pageable pageable, String sort, String order, String keyword);
}
