package com.a404.duckonback.domain.upload.dto;

import com.a404.duckonback.domain.upload.entity.UploadPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UploadPresignRequestDTO {
    @NotNull
    private UploadPurpose purpose;

    @NotNull
    private Long refId;

    @NotBlank
    private String contentType;

    @NotBlank
    private String filename;
}
