package com.a404.duckonback.service;

import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.LoginResponseDTO;
import com.a404.duckonback.dto.SignupRequestDTO;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    ResponseEntity<?> signup(SignupRequestDTO dto);
    String refreshAccessToken(String refreshToken);
    void logout(String accessTokenHeader, String refreshTokenHeader);
}
