package com.a404.duckonback.domain.me.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowersResponseDTO {
    private List<FollowerInfoDTO> followers;
}
