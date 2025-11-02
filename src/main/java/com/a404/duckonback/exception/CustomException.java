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

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.data = Collections.emptyMap();
    }

    public CustomException(String message, HttpStatus status, Map<String, Object> data) {
        super(message);
        this.status = status;
        this.data = data != null ? data : Collections.emptyMap();
    }
}
