package com.a404.duckonback.dto;

import lombok.*;

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
    private List<Long> artistList;
}
