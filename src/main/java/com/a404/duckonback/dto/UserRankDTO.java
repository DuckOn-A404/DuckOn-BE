package com.a404.duckonback.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRankDTO {
    private Long id;
    private Long roomCreateCount;
    private String rankLevel;
}
