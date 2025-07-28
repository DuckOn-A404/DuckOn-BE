package com.a404.duckonback.dto;

import com.a404.duckonback.entity.Artist;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistDTO {
    private Integer artistId;
    private String nameEn;
    private String nameKr;
    private LocalDate debutDate;
    private String imgUrl;

    public static ArtistDTO fromEntity(Artist artist) {
        return ArtistDTO.builder()
                .artistId(artist.getArtistId())
                .nameEn(artist.getNameEn())
                .nameKr(artist.getNameKr())
                .debutDate(artist.getDebutDate())
                .imgUrl(artist.getImgUrl())
                .build();
    }
}