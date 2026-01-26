package com.a404.duckonback.common.dto;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<T> items;

    /**
     * Spring Data Page<T> → PageResponse<T> 변환
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(page.getContent())
                .build();
    }

    public static <T> PageResponse<T> from1Base(Page<T> page) {
        return PageResponse.<T>builder()
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(page.getContent())
                .build();
    }
}
