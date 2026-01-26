package com.a404.duckonback.domain.user.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowingResponseDTO {
    private List<FollowingInfoDTO> following;
}
