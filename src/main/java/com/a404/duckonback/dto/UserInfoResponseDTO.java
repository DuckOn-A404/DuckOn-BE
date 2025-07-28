package com.a404.duckonback.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResponseDTO {
    private String userId;
    private String nickname;
    private String imgUrl;
    private boolean isFollowing; // 팔로우 여부
    private int followerCount; // 팔로워 수
    private int followingCount; // 팔로잉 수

}
