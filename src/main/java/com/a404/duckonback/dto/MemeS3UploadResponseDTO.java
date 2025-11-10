package com.a404.duckonback.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemeS3UploadResponseDTO {
    private String key;      // S3에 저장된 경로
    private String cdnUrl;   // CloudFront로 접근 가능한 전체 URL
}
