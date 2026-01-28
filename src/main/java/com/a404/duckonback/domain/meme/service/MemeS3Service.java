package com.a404.duckonback.domain.meme.service;

import com.a404.duckonback.domain.meme.dto.MemeS3UploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MemeS3Service {
    MemeS3UploadResponseDTO uploadMeme(MultipartFile multipartFile);
    void deleteMeme(String s3Key);
}
