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
    private long followerCount;

    public static ArtistDTO fromEntity(Artist a, long followerCount) {
        return ArtistDTO.builder()
                .artistId(a.getArtistId())
                .nameEn(a.getNameEn())
                .nameKr(a.getNameKr())
                .debutDate(a.getDebutDate())
                .imgUrl(a.getImgUrl())
                .followerCount(followerCount)
                .build();
    }
}