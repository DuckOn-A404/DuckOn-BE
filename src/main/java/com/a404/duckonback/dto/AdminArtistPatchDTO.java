package com.a404.duckonback.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminArtistPatchDTO {
    private String nameEn;
    private String nameKr;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate debutDate;
    private MultipartFile image;
}