package com.a404.duckonback.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequestDTO {
    private String nickname;
    private String language;
    private String newPassword;
    private MultipartFile profileImg;
}
