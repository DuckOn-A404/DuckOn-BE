package com.a404.duckonback.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminSubjectPatchDTO {

    private Long domainId;                 // 선택
    private Long primaryCategoryId;        // 선택
    private List<Long> categoryIds;        // 전체 교체/덮어쓰기 정책이면 서비스에서 처리

    private String nativeLocale;           // 선택
    private String countryCode;            // 선택(서버에서 대문자 normalize 권장)

    /** slug는 불변 → 여기선 받지 않음 */

    // 공식명 업데이트(있으면 upsert)
    private String englishName;
    private String koreanName;
    private String nativeName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate debutDate;

    private MultipartFile image;           // 새 이미지 업로드 시 교체
}
