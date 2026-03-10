package com.a404.duckonback.domain.me.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequestDTO {
    @Size(min = 1, max = 15, message = "닉네임은 1~15자 사이여야 합니다.")
    private String nickname;

    private String language;
    private MultipartFile profileImg;
}
