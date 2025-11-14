package com.a404.duckonback.service;

public interface S3ValidationService {
    boolean existsInS3(String objectKey);
}
