package com.a404.duckonback.dto;

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
