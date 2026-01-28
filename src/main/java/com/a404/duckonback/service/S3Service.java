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
     * 1) 기본 업로드: 파일 타입(image/audio/…)에 따라
     *    image 파일이면 image/, audio 파일이면 audio/… 폴더에 저장
     */
    public String uploadFile(MultipartFile file) {
        String fileType = determineFileType(file);
        return uploadFile(file, fileType);
    }

    /**
     * 2) prefix 지정 업로드
     */
    public String uploadFile(MultipartFile file, String prefix) {
        String uuidPart = UUID.randomUUID().toString();
        String original = file.getOriginalFilename();
        String key = (prefix == null || prefix.isBlank() ? "" : prefix + "/")
                + uuidPart + "_" + original;

        try {
            PutObjectRequest por = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
//                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(por, RequestBody.fromBytes(file.getBytes()));

            return s3Client.utilities()
                    .getUrl(b -> b.bucket(bucketName).key(key))
                    .toExternalForm();

        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }

    /**
     * 3) 프로필 전용 업로드: profile/{userId}/{fileType}
     */
    public String uploadProfileFile(MultipartFile file, String userId, String fileType) {
        String prefix = String.format("profile/%s/%s", userId, fileType);
        return uploadFile(file, prefix);
    }

    /**
     * URL에서 S3 key만 추출해서 삭제
     */
    public void deleteFile(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();              // ex) "/image/uuid_파일명.png"
            String key = path.startsWith("/")
                    ? path.substring(1)
                    : path;

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

        } catch (URISyntaxException e) {
            throw new RuntimeException("잘못된 파일 URL", e);
        }
    }

    /**
     * 파일의 Content-Type 으로 타입 결정 (image, audio, video, application 등)
     */
    private String determineFileType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null) return "others";
        if (ct.startsWith("image"))       return "image";
        if (ct.startsWith("audio"))       return "audio";
        if (ct.startsWith("video"))       return "video";
        if (ct.startsWith("application")) return "application";
        return "others";
    }
}
