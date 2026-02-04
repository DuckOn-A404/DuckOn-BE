package com.a404.duckonback.domain.upload.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadPresignResponseDTO {
    private String key;
    private String uploadUrl;
    private String fileUrl;
    private long expiresInSeconds;
}
