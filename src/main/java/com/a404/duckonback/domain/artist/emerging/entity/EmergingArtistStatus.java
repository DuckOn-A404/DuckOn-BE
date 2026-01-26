package com.a404.duckonback.domain.artist.emerging.entity;

public enum EmergingArtistStatus {
    ACTIVE, // emerging 목록에 노출
    PROMOTED, // 메인으로 승격된 상태(emerging 목록에선 숨김)
    DELETED // 관리자 삭제 (soft delete)
}
