package com.a404.duckonback.service;

import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.LoginResponseDTO;
import com.a404.duckonback.dto.UserDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.repository.ArtistRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final ArtistService artistService;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        String email = loginRequest.getEmail();
        String userId = loginRequest.getUserId();
        String password = loginRequest.getPassword();

        if ((email == null || email.isBlank()) && (userId == null || userId.isBlank())) {
            throw new RuntimeException("email 또는 userId 중 하나는 필수입니다.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("비밀번호는 필수 입력입니다.");
        }

        User user = null;
        if (email != null && !email.isBlank()) {
            user = userService.findByEmail(email);
        } else if (userId != null && !userId.isBlank()) {
            user = userService.findByUserId(userId);
        }

        if (user == null) {
            throw new RuntimeException("없는 사용자입니다.");
        }

        //비밀번호 일치 확인 ( 암호화 )
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호 불일치");
        }

        //토근 생성
        String accessToken = "";
        String refreshToken = "";

        UserDTO userDTO = UserDTO.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name())
                .language(user.getLanguage())
                .imgUrl(user.getImgUrl())
                .artistList(artistService.findAllArtistIdByUserUuid(user.getUuid()))
                .build();

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }
}
