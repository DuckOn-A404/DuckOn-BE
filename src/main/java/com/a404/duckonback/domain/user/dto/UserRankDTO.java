package com.a404.duckonback.domain.user.dto;

import com.a404.duckonback.common.enums.RankLevel;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRankDTO {
    private Long roomCreateCount;
    private RankLevel rankLevel;
}
