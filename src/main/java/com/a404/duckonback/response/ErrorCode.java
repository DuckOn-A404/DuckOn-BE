package com.a404.duckonback.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    //400 BAD REQUEST
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    UPLOAD_FILE_COUNT_EXCEEDED(400, HttpStatus.BAD_REQUEST, "업로드 가능한 파일 개수를 초과했습니다. 한 번에 최대 3개까지 업로드할 수 있어요."),
    UPLOAD_INVALID_MULTIPART(400, HttpStatus.BAD_REQUEST, "잘못된 파일 업로드 요청입니다."),
    ROOM_BANNED_USER(400,HttpStatus.BAD_REQUEST,"강퇴된 사용자입니다. 입장할 수 없습니다."),


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
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    MEME_NOT_FOUND(404, HttpStatus.NOT_FOUND, "밈을 찾을 수 없습니다."),

    //413 PAYLOAD TOO LARGE
    UPLOAD_SIZE_EXCEEDED(413, HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 용량을 초과했습니다."),


    //429 TOO MANY REQUESTS
    TOO_MANY_REQUESTS(429, HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과하였습니다."),

    //500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    ;



    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
