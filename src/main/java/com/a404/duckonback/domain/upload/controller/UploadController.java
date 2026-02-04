package com.a404.duckonback.domain.upload.controller;

import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.upload.dto.UploadPresignRequestDTO;
import com.a404.duckonback.domain.upload.dto.UploadPresignResponseDTO;
import com.a404.duckonback.domain.upload.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UploadController {

    private final UploadService uploadService;

    @Operation(
            summary = "S3 Presigned PUT URL 발급",
            description = "프론트가 S3로 직접 PUT 업로드할 presigned URL을 발급합니다."
    )
    @PostMapping("/presign")
    public ResponseEntity<ApiResponseDTO<UploadPresignResponseDTO>> presign(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UploadPresignRequestDTO req
    ){
        UploadPresignResponseDTO res = uploadService.createPresignedPut(req, principal.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.PRESIGNED_URL_CREATION_SUCCESS, res));
    }
}
