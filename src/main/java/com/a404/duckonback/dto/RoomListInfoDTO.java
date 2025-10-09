package com.a404.duckonback.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomListInfoDTO {
    private Long roomId;

    /** 주체 PK */
    private Long subjectId;

    /** FE 라우팅용 불변 슬러그 (/subject/:slug) */
    private String subjectSlug;

    /** 화면 표시명(요청 로케일 우선, 없으면 native 등 정책) */
    private String subjectDisplayName;

    private String title;

    /** 호스트 사용자 식별자(로그인 ID, PK 아님) */
    private String hostId;
    private String hostNickname;
    private String hostProfileImgUrl;

    private String imgUrl;
    private int participantCount;
}
