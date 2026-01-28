package com.a404.duckonback.domain.user.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendUsersResponseDTO {
    private List<RecommendedUserDTO> users;
}
