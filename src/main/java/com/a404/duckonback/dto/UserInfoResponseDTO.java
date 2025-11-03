package com.a404.duckonback.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResponseDTO {
    private String userId;
    private String nickname;
    private String imgUrl;
    private boolean following; // 팔로우 여부
    private int followerCount; // 팔로워 수
    private int followingCount; // 팔로잉 수

    private List<RoomSummaryDTO> roomList;          // 과거 히스토리
    private RoomListInfoDTO activeRoom;      // 현재 레디스 라이브(없으면 null)
    private UserRankDTO userRank;               // 유저 랭크 정보

}
