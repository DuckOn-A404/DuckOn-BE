package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class FollowedArtistDTO {
    private Long artistId;
    private String nameEn;
    private String nameKr;
    private LocalDate debutDate;
    private String imgUrl;
}
