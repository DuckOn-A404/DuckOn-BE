package com.a404.duckonback.exception;


import com.a404.duckonback.response.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, Object> data;
    private final ErrorCode errorCode;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.data = Collections.emptyMap();
        this.errorCode = null;
    }

    public CustomException(String message, HttpStatus status, Map<String, Object> data) {
        super(message);
        this.status = status;
        this.data = data != null ? data : Collections.emptyMap();
        this.errorCode = null;
    }

    public CustomException(String message, ErrorCode errorCode) {
        super(message);
        this.status = errorCode.getHttpStatus();
        this.data = Collections.emptyMap();
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = errorCode.getHttpStatus();
        this.data = Collections.emptyMap();
    }
}
