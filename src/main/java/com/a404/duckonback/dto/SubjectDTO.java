package com.a404.duckonback.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SubjectDTO {
    private Long subjectId;
    private String nameEn;
    private String nameKr;
    private LocalDate debutDate;
    private String imgUrl;
    private long followerCount;
}
