package com.a404.duckonback.dto;

import com.a404.duckonback.entity.Category;

public record SubjectCategoryLiteDTO(Long id, String code, String name) {
    public static SubjectCategoryLiteDTO of(Category c) {
        if (c == null) return null;
        return new SubjectCategoryLiteDTO(c.getCategoryId(), c.getCode(), c.getName());
    }
}
