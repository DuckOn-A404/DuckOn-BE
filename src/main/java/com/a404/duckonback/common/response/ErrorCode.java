package com.a404.duckonback.common.response;


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
    TRANSLATION_TEXT_EMPTY(400, HttpStatus.BAD_REQUEST, "번역할 텍스트가 없습니다."),
    TRANSLATION_UNSUPPORTED_LANG(400, HttpStatus.BAD_REQUEST, "지원하지 않는 언어 코드입니다."),
    EXCEED_TOTAL_PAGES(400,HttpStatus.BAD_REQUEST,"존재하는 총 페이지 수보다 큰 페이지 번호입니다."),
    INVALID_REQUEST(400, HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    INVALID_ENUM_VALUE(400,HttpStatus.BAD_REQUEST,"잘못된 ENUM 값이 입력되었습니다."),

    // artist 관련 에러 코드
    ARTIST_NAME_KR_EMPTY(400,HttpStatus.BAD_REQUEST,"아티스트 한글명이 비어있습니다."),
    ARTIST_NAME_EN_EMPTY(400,HttpStatus.BAD_REQUEST,"아티스트 영문명이 비어있습니다."),
    ARTIST_PROFILE_IMAGE_URL_EMPTY(400,HttpStatus.BAD_REQUEST,"아티스트 프로필 이미지 URL이 비어있습니다."),
    DUPLICATE_EMERGING_ARTIST(400,HttpStatus.BAD_REQUEST,"이미 존재하는 라이징 아티스트입니다."),
    SIZE_NOT_VALID(400,HttpStatus.BAD_REQUEST,"잘못된 크기 값입니다."),
    DUPLICATE_REPORT(400,HttpStatus.BAD_REQUEST,"이미 신고한 컨텐츠입니다."),
    ARTIST_INFO_EMPTY(400,HttpStatus.BAD_REQUEST,"아티스트 정보가 비어있습니다."),
    ARTIST_NOT_FOUND(400,HttpStatus.BAD_REQUEST,"존재하지 않는 아티스트입니다."),
    ARTIST_CHANGE_REQUEST_CONTENT_EMPTY(400,HttpStatus.BAD_REQUEST,"아티스트 변경 요청 내용이 비어있습니다."),
    INVALID_TARGET_TYPE(400, HttpStatus.BAD_REQUEST, "targetType 값이 올바르지 않습니다. (ARTIST 또는 EMERGING_ARTIST 중 하나를 입력해주세요.)"),
    INVALID_NEW_ARTIST_IMAGE_REQUEST(400, HttpStatus.BAD_REQUEST, "새로운 아티스트 등록 요청의 경우 refId 값이 필수입니다."),

    // 401 UNAUTHORIZED
    USER_NOT_AUTHENTICATED(401, HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    INVALID_PASSWORD(401, HttpStatus.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다."),
    MISSING_JWT_TOKEN(401, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    REVOKED_JWT_TOKEN(400, HttpStatus.BAD_REQUEST, "사용이 취소된 토큰입니다."),

    // JWT 관련 에러 코드
    INVALID_JWT_TOKEN(401, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_JWT_TOKEN(401, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_CREDENTIALS(401, HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다."),
    EMAIL_VERIFY_FAILED(401,HttpStatus.UNAUTHORIZED,"이메일 인증에 실패했습니다."),


    // 403
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),


    //404 NOT FOUND
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "해당 API를 찾을 수 없습니다."),
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    MEME_NOT_FOUND(404, HttpStatus.NOT_FOUND, "밈을 찾을 수 없습니다."),
    EMERGING_ARTIST_NOT_FOUND(404,HttpStatus.NOT_FOUND,"존재하지 않는 라이징 아티스트입니다."),
    NOT_FOUND_ARTIST(404, HttpStatus.NOT_FOUND, "아티스트를 찾을 수 없습니다."),
    NOTIFICATION_NOT_FOUND(404,HttpStatus.NOT_FOUND,"존재하지 않는 알림입니다."),
    EMERGING_ARTIST_FOLLOW_NOT_FOUND(404,HttpStatus.NOT_FOUND,"라이징 아티스트를 팔로우하고있지 않습니다."),
    REPORT_NOT_FOUND(404,HttpStatus.NOT_FOUND,"존재하지 않는 신고입니다."),

    //409 CONFLICT
    DUPLICATE_ARTIST(409, HttpStatus.CONFLICT, "이미 존재하는 아티스트입니다."),
    ROOM_CREATION_CONFLICT(409, HttpStatus.CONFLICT, "이미 방 생성 요청이 처리 중입니다."),

    //413 PAYLOAD TOO LARGE
    UPLOAD_SIZE_EXCEEDED(413, HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 용량을 초과했습니다."),


    //429 TOO MANY REQUESTS
    TOO_MANY_REQUESTS(429, HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과하였습니다."),
    TOO_MANY_ROOMS(429,HttpStatus.TOO_MANY_REQUESTS,"방은 하나만 만들 수 있습니다."),

    //500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    TRANSLATION_FAILED(500, HttpStatus.INTERNAL_SERVER_ERROR, "번역 중 오류가 발생했습니다."),
    ;



    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
