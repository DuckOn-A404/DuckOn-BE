package com.a404.duckonback.service;

import com.a404.duckonback.dto.MemeS3UploadResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemeS3ServiceImpl implements MemeS3Service{

    private final S3Client s3Client;

    @Value("${meme.s3.bucket}")
    private String bucketName;

    @Value("${meme.cdn.base-url}")
    private String cdnBaseUrl;

    @Override
    public MemeS3UploadResponseDTO uploadMeme(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 예: memes/2025/11/uuid.gif
            String key = String.format("memes/%d/%02d/%s%s",
                    LocalDate.now().getYear(),
                    LocalDate.now().getMonthValue(),
                    UUID.randomUUID(),
                    ext);

            // S3에 업로드
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            // CloudFront CDN URL
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8)
                    .replace("+", "%20"); // 공백 처리
            String cdnUrl = cdnBaseUrl.endsWith("/")
                    ? cdnBaseUrl + encodedKey
                    : cdnBaseUrl + "/" + encodedKey;

            log.info("✅ Meme uploaded to S3: key={}, cdnUrl={}", key, cdnUrl);
            return MemeS3UploadResponseDTO.builder()
                    .key(key)
                    .cdnUrl(cdnUrl)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패", e);
        } catch (S3Exception e) {
            log.error("S3 업로드 실패: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    private String extractExt(String originalFilename) {
        if (originalFilename == null) return "bin";
        int dot = originalFilename.lastIndexOf('.');
        if (dot == -1 || dot == originalFilename.length() - 1) return "bin";
        return originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

}
