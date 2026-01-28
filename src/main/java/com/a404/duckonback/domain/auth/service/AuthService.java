package com.a404.duckonback.domain.auth.service;

import com.a404.duckonback.common.dto.JWTDTO;
import com.a404.duckonback.domain.auth.dto.LoginRequestDTO;
import com.a404.duckonback.domain.auth.dto.LoginResponseDTO;
import com.a404.duckonback.domain.auth.dto.SignupRequestDTO;
import com.a404.duckonback.domain.user.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    ResponseEntity<?> signup(SignupRequestDTO dto);
    void logout(User user, String refreshTokenHeader);
    Map<String, String> refreshJWT(String refreshHeader);
    JWTDTO getJWT(String email);

}
