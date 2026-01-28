package com.a404.duckonback.domain.me.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeRequestDTO {
    String currentPassword;
    String newPassword;
}
