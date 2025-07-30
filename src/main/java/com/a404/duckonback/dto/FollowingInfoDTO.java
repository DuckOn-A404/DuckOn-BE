package com.a404.duckonback.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowingInfoDTO {
    private String nickname;
    private String userId;
    private String profileImgUrl;
}
