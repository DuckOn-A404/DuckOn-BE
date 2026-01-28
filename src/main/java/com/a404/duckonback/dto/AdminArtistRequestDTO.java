package com.a404.duckonback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminArtistRequestDTO {

    @NotBlank
    private String nameEn;

    @NotBlank
    private String nameKr;

    @NotNull
    @PastOrPresent
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate debutDate;

    /**
     * multipart/form-data 로 전송된 이미지 파일.
     * 비어있으면 업로드 없이 넘어갑니다.
     */
    private MultipartFile image;
}