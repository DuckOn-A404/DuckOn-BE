package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlockedUserDTO {
    private String userId;    // 차단 대상자의 회원 아이디
    private String nickname;  // 닉네임
    private String imgUrl;    // 프로필 이미지 URL (nullable)
}