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
    PASSWORD_POLICY_VIOLATION(400, HttpStatus.BAD_REQUEST, "새로운 비밀번호가 보안 정책에 맞지 않습니다."),
    SAME_PASSWORD_NOT_ALLOWED(400, HttpStatus.BAD_REQUEST, "이전과 동일한 비밀번호로는 변경할 수 없습니다."),
    CURRENT_PASSWORD_EMPTY(400, HttpStatus.BAD_REQUEST, "기존 비밀번호가 입력되지 않았습니다"),
    NEW_PASSWORD_EMPTY(400, HttpStatus.BAD_REQUEST, "새로운 비밀번호가 입력되지 않았습니다"),


    // 401 UNAUTHORIZED
    USER_NOT_AUTHENTICATED(401, HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    INVALID_PASSWORD(401, HttpStatus.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다."),

    // JWT 관련 에러 코드
    INVALID_JWT_TOKEN(401, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_JWT_TOKEN(401, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_CREDENTIALS(401, HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다."),


    // 403
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),


    //404 NOT FOUND
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "해당 API를 찾을 수 없습니다."),
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    MEME_NOT_FOUND(404, HttpStatus.NOT_FOUND, "밈을 찾을 수 없습니다."),

    //409 CONFLICT
    ROOM_CREATION_CONFLICT(409, HttpStatus.CONFLICT, "이미 방 생성 요청이 처리 중입니다."),

    //413 PAYLOAD TOO LARGE
    UPLOAD_SIZE_EXCEEDED(413, HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 용량을 초과했습니다."),


    //429 TOO MANY REQUESTS
    TOO_MANY_REQUESTS(429, HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과하였습니다."),
    TOO_MANY_ROOMS(429,HttpStatus.TOO_MANY_REQUESTS,"방은 하나만 만들 수 있습니다."),

    //500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    ;



    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
