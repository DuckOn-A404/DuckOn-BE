package com.a404.duckonback.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Builder
public record ApiResponseDTO<T>(
        @JsonIgnore HttpStatus httpStatus,
        int status,
        @NonNull String message,
        @JsonInclude(value = NON_NULL) T data
) {
    public static <T> ApiResponseDTO<T> success(final SuccessCode successCode, @Nullable final T data) {
        return ApiResponseDTO.<T>builder()
                .httpStatus(successCode.getHttpStatus())
                .status(successCode.getCode())
                .message(successCode.getMessage())
                .data(data)
                .build();
    }

    public static <T> ApiResponseDTO<T> success(final SuccessCode successCode) {
        return ApiResponseDTO.<T>builder()
                .httpStatus(successCode.getHttpStatus())
                .status(successCode.getCode())
                .message(successCode.getMessage())
                .data(null)
                .build();
    }

    public static <T> ApiResponseDTO<T> fail(final ErrorCode errorCode) {
        return ApiResponseDTO.<T>builder()
                .httpStatus(errorCode.getHttpStatus())
                .status(errorCode.getCode())
                .message(errorCode.getMessage())
                .data(null)
                .build();
    }

    /*
     * @Valid 오류 메시지 전달 메서드*/
    public static ApiResponseDTO<?> fail(ErrorCode errorCode, String customMessage) {
        return ApiResponseDTO.builder()
                .httpStatus(errorCode.getHttpStatus())
                .status(errorCode.getCode())
                .message(customMessage) // 커스텀 메시지 적용
                .data(null)
                .build();
    }
}