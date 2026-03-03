package com.a404.duckonback.common.response;

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
    EMAIL_VERIFIED(200,HttpStatus.OK,"이메일 인증이 완료되었습니다."),
    AUTH_REFRESH_SUCCESS(200, HttpStatus.OK, "인증 토큰이 성공적으로 갱신되었습니다."),
    AUTH_LOGOUT_SUCCESS(200, HttpStatus.OK, "로그아웃이 성공적으로 처리되었습니다."),

    // 회원가입 관련
    USER_SIGNUP_SUCCESS(201, HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    EMAIL_AVAILABLE(200, HttpStatus.OK, "사용 가능한 이메일입니다."),

    // 관리자 api 관련
    ADMIN_REBUILD_ENGAGEMENT_SUCCESS(200, HttpStatus.OK, "유저 참여도 지표 재생성에 성공했습니다."),
    ADMIN_BUILD_MEME_HOURLY_TOP10_SUCCESS(200, HttpStatus.OK, "시간별 밈 TOP10 집계가 완료되었습니다."),
    ADMIN_GET_USER_LIST_SUCCESS(200, HttpStatus.OK, "[관리자] 전체 사용자 목록 조회에 성공했습니다."),
    ADMIN_GET_USER_DETAIL_SUCCESS(200, HttpStatus.OK, "[관리자] 사용자 상세 조회에 성공했습니다."),
    ADMIN_SEARCH_USER_SUCCESS(200, HttpStatus.OK, "[관리자] 사용자 검색에 성공했습니다."),
    ADMIN_GET_REPORT_LIST_SUCCESS(200, HttpStatus.OK, "신고 목록 조회에 성공했습니다."),
    ADMIN_GET_ARTIST_LIST_SUCCESS(200, HttpStatus.OK, "[관리자] 전체 아티스트 목록 조회에 성공했습니다."),
    ADMIN_SEARCH_ARTIST_SUCCESS(200, HttpStatus.OK, "[관리자] 아티스트 검색에 성공했습니다."),
    ADMIN_DELETE_ARTIST_SUCCESS(200, HttpStatus.OK, "[관리자] 아티스트 삭제에 성공했습니다."),
    ADMIN_PATCH_ARTIST_SUCCESS(200, HttpStatus.OK, "[관리자] 아티스트 정보 수정에 성공했습니다."),
    ADMIN_GET_ARTIST_CHANGE_REQUEST_LIST_SUCCESS(200, HttpStatus.OK, "[관리자] 아티스트 정보 변경 요청 목록 조회에 성공했습니다."),
    ADMIN_GET_ARTIST_CHANGE_REQUEST_DETAIL_SUCCESS(200, HttpStatus.OK, "[관리자] 아티스트 정보 변경 요청 상세 조회에 성공했습니다."),
    ADMIN_REVIEW_ARTIST_CHANGE_REQUEST_SUCCESS(200, HttpStatus.OK, "[관리자] 아티스트 변경 요청 검토에 성공했습니다."),
    ADMIN_PATCH_EMERGING_ARTIST_SUCCESS(200, HttpStatus.OK, "[관리자] 라이징 아티스트 정보 수정에 성공했습니다."),
    ADMIN_UPDATE_HOME_SEARCH_PLACEHOLDER_SUCCESS(200, HttpStatus.OK, "[관리자] 홈 검색창 플레이스홀더 업데이트에 성공했습니다."),

    // 신고 api 관련
    REPORT_CREATE_SUCCESS(200, HttpStatus.OK, "신고가 성공적으로 저장되었습니다."),
    REPORT_SEARCH_SUCCESS(200, HttpStatus.OK, "신고 검색에 성공했습니다."),
    ADMIN_GET_REPORT_DETAIL_SUCCESS(200, HttpStatus.OK, "신고 상세 조회에 성공했습니다."),
    ADMIN_GET_REPORT_LIST_BY_REPORTER_SUCCESS(200, HttpStatus.OK, "신고자 별 조회에 성공했습니다."),
    ADMIN_GET_REPORT_LIST_BY_REPORTED_SUCCESS(200, HttpStatus.OK, "피신고자 별 조회에 성공했습니다."),
    ADMIN_GET_REPORT_LIST_BY_STATUS_SUCCESS(200, HttpStatus.OK, "신고 상태 별 조회에 성공했습니다."),
    ADMIN_GET_REPORT_LIST_BY_CONTENT_TYPE_SUCCESS(200, HttpStatus.OK, "신고 컨텐츠 유형 별 조회에 성공했습니다."),

    // 밈 api 관련
    MEME_UPLOAD_SUCCESS(200, HttpStatus.OK, "밈 업로드를 성공했습니다."),
    MEME_DELETE_SUCCESS(200, HttpStatus.OK, "밈을 성공적으로 삭제했습니다."),
    MEME_UPDATE_SUCCESS(200, HttpStatus.OK, "밈을 성공적으로 수정했습니다."),
    FILE_S3_UPLOAD_SUCCESS(200, HttpStatus.OK, "파일을 성공적으로 S3에 업로드했습니다."),
    MEME_RETRIEVE_SUCCESS(200, HttpStatus.OK, "밈 조회에 성공했습니다."),
    MEME_USAGE_LOG_SUCCESS(200, HttpStatus.OK, "밈 사용 기록이 성공적으로 저장되었습니다."),
    MEME_FAVORITE_CREATED(200, HttpStatus.OK, "밈을 성공적으로 즐겨찾기했습니다."),
    MEME_FAVORITE_DELETED(200, HttpStatus.OK,"밈 즐겨찾기를 성공적으로 취소하였습니다."),
    MEME_TOP10_RETRIEVE_SUCCESS(200, HttpStatus.OK, "밈 TOP10 조회에 성공했습니다."),
    TAG_TRENDING_RETRIEVE_SUCCESS(200, HttpStatus.OK, "실시간 인기 태그 조회에 성공했습니다."),
    TAG_SEARCH_LOG_SUCCESS(200, HttpStatus.OK, "태그 검색 로그 기록에 성공했습니다."),
    TAG_SEARCH_SUCCESS(200, HttpStatus.OK, "태그 검색에 성공했습니다."),

    // 번역 api 관련
    CHAT_TRANSLATION_SUCCESS(200, HttpStatus.OK, "채팅 번역을 성공적으로 수행했습니다."),

    // 아티스트 관련
    CREATE_EMERGING_ARTIST_SUCCESS(201, HttpStatus.CREATED, "라이징 아티스트 등록에 성공했습니다."),
    GET_EMERGING_ARTIST_LIST_SUCCESS(200, HttpStatus.OK, "라이징 아티스트 목록 조회에 성공했습니다."),
    GET_EMERGING_ARTIST_DETAIL_SUCCESS(200, HttpStatus.OK, "라이징 아티스트 상세 조회에 성공했습니다."),
    FOLLOW_EMERGING_ARTIST_SUCCESS(200, HttpStatus.OK, "라이징 아티스트 팔로우에 성공했습니다."),
    UNFOLLOW_EMERGING_ARTIST_SUCCESS(200, HttpStatus.OK, "라이징 아티스트 언팔로우에 성공했습니다."),

    // 아티스트 정보 수정 요청 관련
    CREATE_ARTIST_CHANGE_REQUEST_SUCCESS(201, HttpStatus.CREATED, "아티스트 변경 요청이 성공적으로 접수되었습니다."),
    GET_MY_ARTIST_CHANGE_REQUEST_LIST_SUCCESS(200, HttpStatus.OK, "아티스트 변경 요청 내역 조회에 성공했습니다."),

    // 알림 관련
    GET_MY_NOTIFICATION_LIST_SUCCESS(200, HttpStatus.OK, "내 알림 목록 조회에 성공했습니다."),
    MARK_NOTIFICATION_AS_READ_SUCCESS(200, HttpStatus.OK, "알림을 읽음 처리하는 데 성공했습니다."),
    GET_NOTIFICATION_DETAIL_SUCCESS(200, HttpStatus.OK, "알림 상세 조회에 성공했습니다."),

    // 파일 업로드 관련
    PRESIGNED_URL_CREATION_SUCCESS(200, HttpStatus.OK, "파일의 presigned url을 생성하는 데 성공했습니다."),

    // 홈 검색창 플레이스홀더 관련
    GET_HOME_SEARCH_PLACEHOLDER_SUCCESS(200, HttpStatus.OK, "홈 검색창 플레이스홀더 조회에 성공했습니다.")
    ;



    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}