package com.a404.duckonback.common.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JWTDTO {
    private String accessToken;
    private String refreshToken;
}
