package com.a404.duckonback.domain.meme.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoriteMemeDTO  {
    private Long memeId;
    private String memeUrl;
    private List<String> tags;
    private LocalDateTime favoritedAt; // 즐겨찾기한 시각
}