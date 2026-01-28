package com.a404.duckonback.domain.meme.dto;

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