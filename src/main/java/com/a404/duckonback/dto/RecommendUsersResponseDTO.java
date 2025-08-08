package com.a404.duckonback.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendUsersResponseDTO {
    private List<RecommendedUserDTO> users;
}
