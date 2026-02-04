package com.a404.duckonback.common.infra.s3.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
            return true;
        } catch(S3Exception e){
            if(e.statusCode() == 404) return false;
            log.error("S3 headObject error: key={}, objectKey={}, error={}",
                     objectKey, e.statusCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("S3 validation error: key={}, error={}", objectKey, e.getMessage());
            return false;
        }
    }
}
