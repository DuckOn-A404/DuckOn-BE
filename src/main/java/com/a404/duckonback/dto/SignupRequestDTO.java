package com.a404.duckonback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(
            regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
            message = "유효한 이메일 형식이 아닙니다."
    )
    private String email;

    @NotBlank(message = "아이디를 입력해주세요.")
    private String userId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,}$",
            message = "영문, 숫자, 특수문자를 각각 1자 이상 포함하고 최소 8자여야 합니다."
    )
    private String password;

    private String nickname;

    @NotBlank(message = "언어를 선택해주세요.")
    private String language;

    private List<Long> artistList;
    private MultipartFile profileImg;
}