package com.a404.duckonback.dto;

import com.a404.duckonback.enums.RankLevel;
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
