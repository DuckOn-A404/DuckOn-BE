package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistDetailDTO {
    private Long artistId;
    private String nameKr;
    private String nameEn;
    private LocalDate debutDate;
    private String imgUrl;
    private boolean isFollowed;
    private LocalDateTime followedAt;

    public static ArtistDetailDTO of(
            com.a404.duckonback.entity.Artist artist,
            boolean isFollowed,
            java.time.LocalDateTime followedAt
    ) {
        ArtistDetailDTO dto = new ArtistDetailDTO();
        dto.setArtistId(artist.getArtistId());
        dto.setNameKr(artist.getNameKr());
        dto.setNameEn(artist.getNameEn());
        dto.setDebutDate(artist.getDebutDate());
        dto.setImgUrl(artist.getImgUrl());
        dto.setFollowed(isFollowed);
        dto.setFollowedAt(followedAt);
        return dto;
    }
}