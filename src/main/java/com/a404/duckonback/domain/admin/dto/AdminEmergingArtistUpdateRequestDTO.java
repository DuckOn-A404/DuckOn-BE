package com.a404.duckonback.domain.admin.dto;

import com.a404.duckonback.common.validation.NullOrNotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEmergingArtistUpdateRequestDTO {
    @NullOrNotBlank(message = "이름(한글)은 공백이 아니어야 합니다.")
    private String nameKr;

    @NullOrNotBlank(message = "이름(영어)은 공백이 아니어야 합니다.")
    private String nameEn;

    private LocalDate debutDate;

    @NullOrNotBlank(message = "이미지 URL은 공백이 아니어야 합니다.")
    private String imgUrl;
}
