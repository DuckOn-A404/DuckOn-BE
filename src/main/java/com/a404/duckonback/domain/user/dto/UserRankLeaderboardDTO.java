package com.a404.duckonback.domain.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRankLeaderboardDTO {
    private String nickname;
    private String userId;
    private String profileImgUrl;
    private UserRankDTO userRank;
}
