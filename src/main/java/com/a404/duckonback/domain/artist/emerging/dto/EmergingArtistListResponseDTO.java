package com.a404.duckonback.domain.artist.emerging.dto;

import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistStatus;
import com.a404.duckonback.domain.artist.emerging.repository.FollowedEmergingArtistRow;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmergingArtistListResponseDTO {
    private Long emergingArtistId;
    private LocalDateTime createdAt;
    private LocalDate debutDate;
    private String nameKr;
    private String nameEn;
    private String imgUrl;
    private EmergingArtistStatus status;
    private Long followerCount;

    public static EmergingArtistListResponseDTO from(FollowedEmergingArtistRow row) {
        return EmergingArtistListResponseDTO.builder()
                .emergingArtistId(row.getEmergingArtistId())
                .createdAt(row.getCreatedAt())
                .debutDate(row.getDebutDate())
                .nameKr(row.getNameKr())
                .nameEn(row.getNameEn())
                .imgUrl(row.getImgUrl())
                .status(row.getStatus())
                .followerCount(row.getFollowerCount())
                .build();
    }
}
