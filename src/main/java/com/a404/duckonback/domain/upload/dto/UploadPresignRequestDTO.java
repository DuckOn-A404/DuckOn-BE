package com.a404.duckonback.domain.upload.dto;

import com.a404.duckonback.domain.upload.entity.UploadPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadPresignRequestDTO {
    @NotNull
    private UploadPurpose purpose;

    private Long refId;

    @NotBlank
    private String contentType;

    @NotBlank
    private String filename;
}
