package com.a404.duckonback.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminSubjectRequestDTO {

    @NotNull
    private Long domainId;

    private Long primaryCategoryId;

    /** 추가로 매핑할 카테고리들(N:M) */
    private List<Long> categoryIds;

    /** 원어 로케일 (예: ko, en, ja …) */
    @NotBlank
    private String nativeLocale;

    /** ISO-3166-1 alpha-2 (KR/JP/US …) */
    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}$", message = "countryCode는 ISO-3166-1 2자리 대문자여야 합니다.")
    private String countryCode;

    /** URL 슬러그(불변). 미입력 시 서버가 영문 공식명 기반으로 생성 */
    @Size(max = 120)
    private String slug;

    /** 영문 공식명 – 필수(슬러그 생성 기준) */
    @NotBlank
    private String englishName;

    /** 선택: 한국어 공식명 */
    private String koreanName;

    /** 선택: 원어 공식명 (nativeLocale 기준) */
    private String nativeName;

    @PastOrPresent
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate debutDate;

    /** 썸네일/대표 이미지 */
    private MultipartFile image;
}
