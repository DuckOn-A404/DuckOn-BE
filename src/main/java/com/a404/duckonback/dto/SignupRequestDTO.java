package com.a404.duckonback.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {
    private String email;
    private String userId;
    private String password;
    private String nickname;
    private String language;
    private List<Long> artistList;
    private MultipartFile profileImg;
}
