package com.a404.duckonback.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ValidationServiceImpl implements S3ValidationService {

    private final S3Client s3Client;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    @Override
    public boolean existsInS3(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.headObject(headObjectRequest);
            log.debug("✅ S3 object exists: {}", objectKey);
            return true;
        } catch (NoSuchKeyException e) {
            log.warn("⚠️ S3 object not found: {}", objectKey);
            return false;
        } catch (Exception e) {
            log.error("❌ S3 validation error: objectKey={}, error={}", 
                     objectKey, e.getMessage());
            return false;
        }
    }
}
