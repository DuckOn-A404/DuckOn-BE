package com.a404.duckonback.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    //400 BAD REQUEST
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),


    // 401 UNAUTHORIZED
    USER_NOT_AUTHENTICATED(401, HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

    // JWT 관련 에러 코드
    INVALID_JWT_TOKEN(401, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_JWT_TOKEN(401, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_CREDENTIALS(401, HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다."),


    // 403
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),


    //404 NOT FOUND
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "해당 API를 찾을 수 없습니다."),

    //429 TOO MANY REQUESTS
    TOO_MANY_REQUESTS(429, HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과하였습니다."),

    //500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    ;



    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
