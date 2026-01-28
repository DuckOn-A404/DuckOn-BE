package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveRoomSummaryDTO {
    private Long roomId;
    private String title;
    private String hostId;
    private String hostNickname;
    private String hostProfileImgUrl; // 호스트 프로필 사진 URL
    private String imgUrl;
    private int participantCount; // 현재 접속자 수
}
