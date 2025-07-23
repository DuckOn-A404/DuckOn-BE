package com.a404.duckonback.service;

import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.LoginResponseDTO;
import com.a404.duckonback.dto.UserDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserServiceImpl userServiceImpl;
    private final ArtistServiceImpl artistServiceImpl;

    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        String email = loginRequest.getEmail();
        String userId = loginRequest.getUserId();
        String password = loginRequest.getPassword();

        if ((email == null || email.isBlank()) && (userId == null || userId.isBlank())) {
            throw new CustomException("email 또는 userId 중 하나는 필수입니다.", HttpStatus.BAD_REQUEST);
        }

        if (password == null || password.isBlank()) {
            throw new CustomException("비밀번호는 필수 입력입니다.", HttpStatus.BAD_REQUEST);
        }

        User user = null;
        if (email != null && !email.isBlank()) {
            user = userServiceImpl.findByEmail(email);
        } else if (userId != null && !userId.isBlank()) {
            user = userServiceImpl.findByUserId(userId);
        }

        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }

        //비밀번호 일치 확인 ( 암호화 )
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        //토근 생성
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        UserDTO userDTO = UserDTO.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name())
                .language(user.getLanguage())
                .imgUrl(user.getImgUrl())
                .artistList(artistServiceImpl.findAllArtistIdByUserUuid(user.getUuid()))
                .build();

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }
}
