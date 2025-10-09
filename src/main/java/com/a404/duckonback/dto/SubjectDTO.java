package com.a404.duckonback.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubjectDTO {
    private Long subjectId;
    private String slug;          // FE 라우팅용
    private String displayName;   // 요청 로케일 → 없으면 native로 대체
    private LocalDate debutDate;
    private String imgUrl;
    private long followerCount;
}
