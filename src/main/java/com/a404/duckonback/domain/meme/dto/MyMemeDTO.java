package com.a404.duckonback.domain.meme.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MyMemeDTO {
    private Long memeId;
    private String imageUrl;
    private LocalDateTime createdAt;
    private int usageCnt;
    private int downloadCnt;
}