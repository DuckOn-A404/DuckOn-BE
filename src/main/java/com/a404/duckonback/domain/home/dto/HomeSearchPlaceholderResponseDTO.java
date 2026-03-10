package com.a404.duckonback.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeSearchPlaceholderResponseDTO {
    private List<String> items;
    private Long version;
    private LocalDateTime updatedAt;
}
