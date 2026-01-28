package com.a404.duckonback.common.infra.s3.validation;

public interface S3ValidationService {
    boolean existsInS3(String objectKey);
}
