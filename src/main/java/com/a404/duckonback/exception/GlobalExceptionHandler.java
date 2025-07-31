package com.a404.duckonback.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", ex.getStatus().value());

        // 추가 데이터가 있다면 포함
        if (ex.getData() != null && !ex.getData().isEmpty()) {
            body.putAll(ex.getData());
        }

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // 개발 중 예외 확인용 로그

        Map<String, Object> body = new HashMap<>();
        body.put("message", "서버 내부 오류가 발생했습니다.");
        body.put("status", 500);

        return ResponseEntity.status(500).body(body);
    }
}
