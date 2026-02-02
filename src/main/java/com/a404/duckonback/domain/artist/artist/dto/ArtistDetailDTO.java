package com.a404.duckonback.domain.artist.artist.dto;

import com.a404.duckonback.domain.artist.artist.entity.Artist;
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
            Artist artist,
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