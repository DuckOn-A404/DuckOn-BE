package com.a404.duckonback.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendedUserDTO {
    private String userId;
    private String nickname;
    private String imgUrl;
    private List<String> reasons;   // includeReasons=true 일 때만 채움
    private UserRankDTO userRank;
}
