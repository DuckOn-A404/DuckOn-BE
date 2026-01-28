package com.a404.duckonback.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeRequestDTO {
    String currentPassword;
    String newPassword;
}
