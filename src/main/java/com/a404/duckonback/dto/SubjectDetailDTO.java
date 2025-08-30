package com.a404.duckonback.dto;

import com.a404.duckonback.entity.Subject;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubjectDetailDTO {
    private Long subjectId;
    private String slug;
    private String displayName;

    private String nativeLocale;
    private String countryCode;

    private LocalDate debutDate;
    private String imgUrl;

    private boolean followed;
    private LocalDateTime followedAt;

    public static SubjectDetailDTO of(Subject s, String displayName, boolean followed, LocalDateTime followedAt) {
        return SubjectDetailDTO.builder()
            .subjectId(s.getId())
            .slug(s.getSlug())
            .displayName(displayName)
            .nativeLocale(s.getNativeLocale())
            .countryCode(s.getCountryCode())
            .debutDate(s.getDebutDate())
            .imgUrl(s.getImgUrl())
            .followed(followed)
            .followedAt(followedAt)
            .build();
    }
}
