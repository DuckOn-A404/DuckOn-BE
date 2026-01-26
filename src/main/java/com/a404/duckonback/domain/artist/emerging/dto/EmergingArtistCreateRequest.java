package com.a404.duckonback.domain.artist.emerging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergingArtistCreateRequest {
    @NotBlank @Size(min = 1, max = 50)
    private String nameKr;
    @NotBlank @Size(min = 1, max = 50)
    private String nameEn;

    private LocalDate debutDate;

    @NotBlank
    private String imgUrl;
}
