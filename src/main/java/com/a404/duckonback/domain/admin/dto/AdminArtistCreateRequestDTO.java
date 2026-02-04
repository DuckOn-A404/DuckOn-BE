package com.a404.duckonback.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminArtistCreateRequestDTO {

    @NotBlank
    private String nameEn;

    @NotBlank
    private String nameKr;

    @NotNull
    @PastOrPresent
    private LocalDate debutDate;

    @NotBlank
    private String imgUrl;
}