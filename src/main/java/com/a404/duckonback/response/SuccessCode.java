package com.a404.duckonback.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    // 유저 관련
    GET_USER_LEADERBOARD_SUCCESS(200, HttpStatus.OK, "유저 리더보드 조회에 성공했습니다."),
    PASSWORD_CHANGE_SUCCESS(200, HttpStatus.OK, "비밀번호를 성공적으로 변경했습니다."),
    GET_USER_ROOM_CREATE_HISTORY_SUCCESS(200, HttpStatus.OK, "유저의 방 생성 기록을 성공적으로 불러왔습니다."),
    GET_USER_MEME_CREATE_HISTORY_SUCCESS(200, HttpStatus.OK, "유저의 밈 생성 기록을 성공적으로 불러왔습니다."),

    // 회원가입 관련
    USER_SIGNUP_SUCCESS(201, HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    EMAIL_AVAILABLE(200, HttpStatus.OK, "사용 가능한 이메일입니다."),

    // 관리자 api 관련
    ADMIN_REBUILD_ENGAGEMENT_SUCCESS(200, HttpStatus.OK, "유저 참여도 지표 재생성에 성공했습니다."),
    ADMIN_BUILD_MEME_HOURLY_TOP10_SUCCESS(200, HttpStatus.OK, "시간별 밈 TOP10 집계가 완료되었습니다."),

    // 밈 api 관련
    MEME_UPLOAD_SUCCESS(200, HttpStatus.OK, "밈 업로드를 성공했습니다."),
    MEME_DELETE_SUCCESS(200, HttpStatus.OK, "밈을 성공적으로 삭제했습니다."),
    FILE_S3_UPLOAD_SUCCESS(200, HttpStatus.OK, "파일을 성공적으로 S3에 업로드했습니다."),
    MEME_RETRIEVE_SUCCESS(200, HttpStatus.OK, "밈 조회에 성공했습니다."),
    MEME_USAGE_LOG_SUCCESS(200, HttpStatus.OK, "밈 사용 기록이 성공적으로 저장되었습니다."),
    MEME_FAVORITE_CREATED(200, HttpStatus.OK, "밈을 성공적으로 즐겨찾기했습니다."),
    MEME_FAVORITE_DELETED(200, HttpStatus.OK,"밈 즐겨찾기를 성공적으로 취소하였습니다."),
    MEME_TOP10_RETRIEVE_SUCCESS(200, HttpStatus.OK, "밈 TOP10 조회에 성공했습니다."),
    TAG_TRENDING_RETRIEVE_SUCCESS(200, HttpStatus.OK, "실시간 인기 태그 조회에 성공했습니다."),
    TAG_SEARCH_LOG_SUCCESS(200, HttpStatus.OK, "태그 검색 로그 기록에 성공했습니다."),
    ;



    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}