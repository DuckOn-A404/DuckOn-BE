package com.a404.duckonback.common.enums;

public enum RequestStatus {
    PENDING,   // 검토 대기
    APPROVED,  // 승인(적용 전 단계로 두고 싶을 때)
    REJECTED,  // 반려
    CANCELED,  // 요청자 취소
    APPLIED    // 실제 artist/emerging에 반영 완료
}
