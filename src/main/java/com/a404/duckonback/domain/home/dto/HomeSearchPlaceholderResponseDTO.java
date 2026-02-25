package com.a404.duckonback.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class HomeSearchPlaceholderResponseDTO {
    private List<String> items;
    private Long version;
    private LocalDateTime updatedAt;
}
