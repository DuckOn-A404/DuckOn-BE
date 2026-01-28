package com.a404.duckonback.domain.admin.dto;

import java.time.LocalDate;

import com.a404.duckonback.domain.artist.entity.Artist;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminArtistListDTO {
    private Long artistId;
    private String nameKr;
    private String nameEn;
    private LocalDate debutDate;
    private String imgUrl;

    public static AdminArtistListDTO fromEntity(Artist artist) {
        return AdminArtistListDTO.builder()
                .artistId(artist.getArtistId())
                .nameKr(artist.getNameKr())
                .nameEn(artist.getNameEn())
                .debutDate(artist.getDebutDate())
                .imgUrl(artist.getImgUrl())
                .build();
    }
}
