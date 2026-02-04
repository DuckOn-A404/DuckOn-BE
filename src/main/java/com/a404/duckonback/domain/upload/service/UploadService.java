package com.a404.duckonback.domain.upload.service;

import com.a404.duckonback.domain.upload.dto.UploadPresignRequestDTO;
import com.a404.duckonback.domain.upload.dto.UploadPresignResponseDTO;

public interface UploadService {
    UploadPresignResponseDTO createPresignedPut(UploadPresignRequestDTO req, Long userId);
}
