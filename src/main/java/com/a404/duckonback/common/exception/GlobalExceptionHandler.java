package com.a404.duckonback.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.ErrorCode;
import org.apache.tomcat.util.http.fileupload.impl.FileCountLimitExceededException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleCustomException(CustomException ex) {
        ErrorCode code = ex.getErrorCode();

        // ErrorCode가 명확히 매핑되는 경우
        if(code != null){
            return ResponseEntity
                    .status(code.getHttpStatus())
                    .body(ApiResponseDTO.fail(code));
        }

        // ErrorCode가 없는 경우, 기본적으로 INVALID_REQUEST로 처리
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponseDTO.fail(ErrorCode.INVALID_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("잘못된 요청입니다.");

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponseDTO.fail(ErrorCode.INVALID_REQUEST, message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBindException(BindException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("잘못된 요청입니다.");

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponseDTO.fail(ErrorCode.INVALID_REQUEST, message));
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable root = getRootCause(ex);

        // 1. Enum 타입 불일치
        if(root instanceof InvalidFormatException ife){
            // 어떤 필드에서 발생했는지 확인
            String fieldName = null;
            if(ife.getPath() != null && !ife.getPath().isEmpty()){
                fieldName = ife.getPath().get(0).getFieldName();
            }

            // targetType이 error인 경우
            if("targetType".equals(fieldName)){
                return ResponseEntity
                        .status(ErrorCode.INVALID_TARGET_TYPE.getHttpStatus())
                        .body(ApiResponseDTO.fail(ErrorCode.INVALID_TARGET_TYPE));
            }

            // 그 외 필드의 enum 불일치
            return ResponseEntity
                    .status(ErrorCode.INVALID_ENUM_VALUE.getHttpStatus())
                    .body(ApiResponseDTO.fail(ErrorCode.INVALID_ENUM_VALUE));
        }
        // JSON 자체가 깨진 경우(파싱 불가)
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponseDTO.fail(ErrorCode.INVALID_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleGenericException(Exception ex) {
        log.error("[GlobalExceptionHandler] Unhandled exception: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponseDTO.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private Throwable getRootCause(Throwable t) {
        Throwable result = t;
        while (result.getCause() != null && result.getCause() != result) {
            result = result.getCause();
        }
        return result;
    }
}
