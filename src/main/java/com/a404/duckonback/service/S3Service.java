package com.a404.duckonback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    /**
     * 1) 기본 업로드 (루트에 바로)
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "");
    }

    /**
     * 2) 폴더(prefix) 지정 업로드
     *    ex) uploadFile(file, "users/123"), uploadFile(file, "profile/image")
     */
    public String uploadFile(MultipartFile file, String prefix) {
        String uuidPart = UUID.randomUUID().toString();
        String filename = file.getOriginalFilename();
        String key = (prefix.isBlank() ? "" : prefix + "/")
                + uuidPart + "_" + filename;

        try {
            PutObjectRequest por = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(por, RequestBody.fromBytes(file.getBytes()));

            // 퍼블릭 URL 반환
            return s3Client.utilities()
                    .getUrl(b -> b.bucket(bucketName).key(key))
                    .toExternalForm();

        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }

    /**
     * 3) profile/{userId}/{fileType} 구조로 업로드
     *    ex) uploadProfileFile(file, "user123", "image")
     */
    public String uploadProfileFile(MultipartFile file, String userId, String fileType) {
        String prefix = String.format("profile/%s/%s", userId, fileType);
        return uploadFile(file, prefix);
    }

    /**
     * URL에서 key만 뽑아서 삭제
     */
    public void deleteFile(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            // e.g. "/profile/user123/image/uuid_file.jpg"
            String path = uri.getPath();
            // 맨 앞의 "/" 제거
            String key = path.startsWith("/") ? path.substring(1) : path;

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

        } catch (URISyntaxException e) {
            throw new RuntimeException("잘못된 파일 URL", e);
        }
    }
}
