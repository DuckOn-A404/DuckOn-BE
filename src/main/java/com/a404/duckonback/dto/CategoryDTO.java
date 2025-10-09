package com.a404.duckonback.dto;

// 카테고리(트리/플랫 공용)
public record CategoryDTO(
    Long id,
    String code,
    String name,
    int depth,
    Long parentId
) {}
