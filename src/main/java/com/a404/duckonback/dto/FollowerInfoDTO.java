package com.a404.duckonback.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowerInfoDTO {
    private String nickname;
    private String userId;
    private boolean following;
    private String profileImgUrl;
}
