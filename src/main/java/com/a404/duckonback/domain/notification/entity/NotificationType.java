package com.a404.duckonback.domain.notification.entity;

// 사용자에게 보여줄 알림 유형(UI 기준)
public enum NotificationType {
    ARTIST_CHANGE_REQUEST, // 아티스트 변경 요청 처리 결과
    REPORT_RESULT, // 신고 처리 결과
    PENALTY, // 제재 알림
    SYSTEM_ALERT // 시스템 알림
}
