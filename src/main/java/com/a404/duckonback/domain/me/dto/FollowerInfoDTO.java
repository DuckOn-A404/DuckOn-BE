package com.a404.duckonback.domain.me.dto;

import com.a404.duckonback.domain.user.dto.UserRankDTO;
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
    private UserRankDTO userRankDTO;
}
