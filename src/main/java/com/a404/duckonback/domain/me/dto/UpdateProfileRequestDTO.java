package com.a404.duckonback.domain.me.dto;

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
    private MultipartFile profileImg;
}
