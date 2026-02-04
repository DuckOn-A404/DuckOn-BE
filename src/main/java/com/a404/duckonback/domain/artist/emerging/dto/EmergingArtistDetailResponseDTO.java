package com.a404.duckonback.domain.artist.emerging.dto;

import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmergingArtistDetailResponseDTO {
    private Long emergingArtistId;
    private LocalDateTime createdAt;
    private LocalDate debutDate;
    private String nameKr;
    private String nameEn;
    private String imgUrl;
    private EmergingArtistStatus status;
    private String createdByUserNickName;
    private Long followerCount;
    private boolean following;
    private LocalDateTime updatedAt;
}
