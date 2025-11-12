package com.a404.duckonback.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MemeDetailDTO {
    private Long memeId;
    private String imageUrl;
    private LocalDateTime createdAt;
    private int usageCnt;
    private int downloadCnt;
    private int favoriteCnt;

    private MemeCreatorDTO creator;
    private List<String> tags;
}