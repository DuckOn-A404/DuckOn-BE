package com.a404.duckonback.service;

import com.a404.duckonback.dto.JWTDTO;
import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.LoginResponseDTO;
import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    ResponseEntity<?> signup(SignupRequestDTO dto);
    void logout(User user, String refreshTokenHeader);
    Map<String, String> refreshJWT(String refreshHeader);
    JWTDTO getJWT(String email);

}
