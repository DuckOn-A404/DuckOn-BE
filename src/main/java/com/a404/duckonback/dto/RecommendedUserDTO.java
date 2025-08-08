package com.a404.duckonback.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendedUserDTO {
    private String userId;
    private String nickname;
    private String imgUrl;
    private int mutualFollows;      // 나와 겹치는 팔로우 수
    private List<String> reasons;   // includeReasons=true 일 때만 채움
}
