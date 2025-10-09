package com.a404.duckonback.dto;

import java.util.List;

// 도메인 + 그 도메인의 카테고리 트리
public record TaxonomyDTO(
    DomainDTO domain,
    List<CategoryTreeDTO> roots
) {}
