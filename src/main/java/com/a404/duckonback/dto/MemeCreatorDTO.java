package com.a404.duckonback.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MemeCreatorDTO {
    private Long id;
    private String userId;
    private String nickname;
    private String imgUrl;
}