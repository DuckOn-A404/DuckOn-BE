package com.a404.duckonback.dto;

import com.a404.duckonback.enums.UserRole;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserListDTO {
    private Long id;
    private String email;
    private String userId;
    private String nickname;
    private UserRole role;
    private LocalDateTime jointedAt;
    private Instant lastLoginAt;
    private boolean deleted;
    private LocalDateTime deletedAt;
}
