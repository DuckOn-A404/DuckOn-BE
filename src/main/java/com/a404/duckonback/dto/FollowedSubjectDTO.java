package com.a404.duckonback.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FollowedSubjectDTO {
    private Long subjectId;
    private String slug;
    private String displayName;
    private LocalDate debutDate;
    private String imgUrl;
}
