package com.a404.duckonback.dto;

// 트리 구조
public record CategoryTreeDTO(
    Long id,
    String code,
    String name,
    int depth,
    java.util.List<CategoryTreeDTO> children
) {}