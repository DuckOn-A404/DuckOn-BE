package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RoomSummaryDTO {
    private Long roomId;
    private boolean active;
    private String title;
    private String imgUrl;
    private LocalDateTime createdAt;
    private String artistNameKr;
    private String artistNameEn;
    private Long creatorId;
    private Long artistId;

    // JPQL 생성자 (active 기본 false)
    public RoomSummaryDTO(
            Long roomId,
            String title,
            String imgUrl,
            LocalDateTime createdAt,
            String artistNameKr,
            String artistNameEn,
            Long creatorId,
            Long artistId
    ) {
        this.roomId = roomId;
        this.title = title;
        this.imgUrl = imgUrl;
        this.createdAt = createdAt;
        this.artistNameKr = artistNameKr;
        this.artistNameEn = artistNameEn;
        this.creatorId = creatorId;
        this.artistId = artistId;
        this.active = false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
