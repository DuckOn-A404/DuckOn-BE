package com.a404.duckonback.domain.admin.dto;

import com.a404.duckonback.common.enums.UserRole;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSummaryDTO {
    private Long id;
    private String userId;
    private String nickname;
    private UserRole role;
    private String imgUrl;
}
