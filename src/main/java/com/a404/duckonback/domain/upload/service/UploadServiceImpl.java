package com.a404.duckonback.domain.upload.service;

import com.a404.duckonback.common.enums.UserRole;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.upload.dto.UploadPresignRequestDTO;
import com.a404.duckonback.domain.upload.dto.UploadPresignResponseDTO;
import com.a404.duckonback.domain.upload.entity.UploadPurpose;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadServiceImpl implements  UploadService {
    private final S3Presigner s3Presigner;
    private final UserRepository userRepository;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    @Value("${CDN_BASE_URL}")
    private String cdnBaseUrl;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long EXPIRATION_SECONDS = 300; // 5 minutes

    @Override
    public UploadPresignResponseDTO createPresignedPut(UploadPresignRequestDTO req, Long userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId);
        if(user == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        validate(req, isAdmin);

        String ext = extractExtension(req.getFilename());
        String key = buildKey(req.getPurpose(), req.getRefId(), ext);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(req.getContentType())
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(EXPIRATION_SECONDS))
                        .putObjectRequest(putObjectRequest)
                        .build()
        );

        String fileUrl = buildCdnUrl(key);

        return UploadPresignResponseDTO.builder()
                .key(key)
                .uploadUrl(presigned.url().toString())
                .fileUrl(fileUrl)
                .expiresInSeconds(EXPIRATION_SECONDS)
                .build();
    }

    private void validate(UploadPresignRequestDTO req, boolean isAdmin) {
        if (!ALLOWED_CONTENT_TYPES.contains(req.getContentType())) {
            throw new CustomException(ErrorCode.UPLOAD_INVALID_MULTIPART);
        }
        if(req.getPurpose() == UploadPurpose.ARTIST_IMAGE_TEMP && !isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        // 새로운 아티스트 이미지 업로드 시에만 refId가 null이 될 수 있도록 허용
        if(req.getPurpose() != UploadPurpose.ARTIST_IMAGE_TEMP && (req.getRefId() == null || req.getRefId() <= 0)) {
            throw new CustomException(ErrorCode.INVALID_NEW_ARTIST_IMAGE_REQUEST);
        }
    }

    private String buildKey(UploadPurpose purpose, Long refId, String ext) {
        return purpose.getBasePrefix() + "/" + refId + "/" + UUID.randomUUID() + ext;
    }

    private String extractExtension(String filename) {
        if(filename == null) {
            return "bin";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "bin";
        }
        return "." + filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private String buildCdnUrl(String key) {
        // key 예: uploads/artist-change/2/uuid.png
        if (cdnBaseUrl.endsWith("/")) {
            return cdnBaseUrl + key;
        }
        return cdnBaseUrl + "/" + key;
    }
}
