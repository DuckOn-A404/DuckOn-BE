package com.a404.duckonback.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    // 회원가입 관련
    USER_SIGNUP_SUCCESS(201, HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    EMAIL_AVAILABLE(200, HttpStatus.OK, "사용 가능한 이메일입니다."),

    ;



    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}