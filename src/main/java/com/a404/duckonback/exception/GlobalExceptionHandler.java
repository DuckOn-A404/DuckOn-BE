package com.a404.duckonback.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.ErrorCode;
import org.apache.tomcat.util.http.fileupload.impl.FileCountLimitExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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

    // 파일 용량 초과 (Spring이 던짐)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(ErrorCode.UPLOAD_SIZE_EXCEEDED.getHttpStatus())
                .body(ApiResponseDTO.fail(ErrorCode.UPLOAD_SIZE_EXCEEDED));

    }

    // multipart 파싱 관련 (Tomcat/FileUpload 쪽)
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMultipartException(MultipartException ex) {
        Throwable root = getRootCause(ex);

        ErrorCode code;
        if (root instanceof FileCountLimitExceededException) {
            // 파일/파트 개수 초과
            code = ErrorCode.UPLOAD_FILE_COUNT_EXCEEDED;
        } else {
            // 기타 multipart 에러
            code = ErrorCode.UPLOAD_INVALID_MULTIPART;
        }

        log.warn("[MultipartException] message={}, rootCause={}",
                ex.getMessage(),
                root != null ? root.getClass().getName() : "null");

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponseDTO.fail(code));
    }

    private Throwable getRootCause(Throwable t) {
        Throwable result = t;
        while (result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }
}
