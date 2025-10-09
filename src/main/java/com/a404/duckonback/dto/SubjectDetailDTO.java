package com.a404.duckonback.dto;

import com.a404.duckonback.entity.Subject;
import java.util.List;
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

    // 옵션 필드
    private String domainCode;                        // 예: MUSIC / SPORTS / CREATOR
    private SubjectCategoryLiteDTO primaryCategory;   // 대표 카테고리
    private List<SubjectCategoryLiteDTO> categories;  // 매핑된 전체 카테고리

    public static SubjectDetailDTO of(Subject s, String displayName,
        boolean followed, LocalDateTime followedAt,
        boolean includeTaxonomy) {
        SubjectDetailDTO.SubjectDetailDTOBuilder b = SubjectDetailDTO.builder()
            .subjectId(s.getId())
            .slug(s.getSlug())
            .displayName(displayName)
            .nativeLocale(s.getNativeLocale())
            .countryCode(s.getCountryCode())
            .debutDate(s.getDebutDate())
            .imgUrl(s.getImgUrl())
            .followed(followed)
            .followedAt(followedAt);

        if (includeTaxonomy) {
            b.domainCode(s.getDomain() != null ? s.getDomain().getCode() : null)
                .primaryCategory(SubjectCategoryLiteDTO.of(s.getPrimaryCategory()))
                .categories(s.getCategories().stream().map(SubjectCategoryLiteDTO::of).toList());
        }
        return b.build();
    }
}
