package com.a404.duckonback.domain.user.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String email;
    private String userId;
    private String nickname;
    private LocalDateTime createdAt;
    private String role;
    private String language;
    private String imgUrl;
    private Instant lastLoginAt;
    private List<Long> artistList;
}
