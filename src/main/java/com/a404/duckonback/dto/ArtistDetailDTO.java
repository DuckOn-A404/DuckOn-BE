package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    public static ArtistDetailDTO of(com.a404.duckonback.entity.Artist artist, boolean isFollowed) {
        return new ArtistDetailDTO(
                artist.getArtistId(),
                artist.getNameKr(),
                artist.getNameEn(),
                artist.getDebutDate(),
                artist.getImgUrl(),
                isFollowed
        );
    }
}