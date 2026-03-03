package com.a404.duckonback.domain.auth.service;

import com.a404.duckonback.common.dto.JWTDTO;
import com.a404.duckonback.domain.auth.dto.LoginRequestDTO;
import com.a404.duckonback.domain.auth.dto.LoginResponseDTO;
import com.a404.duckonback.domain.auth.dto.RefreshResponseDTO;
import com.a404.duckonback.domain.auth.dto.SignupRequestDTO;
import com.a404.duckonback.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    ResponseEntity<?> signup(SignupRequestDTO dto);
    void logout(User user, HttpServletRequest request, HttpServletResponse response);
    RefreshResponseDTO refreshJWT(HttpServletRequest request, HttpServletResponse response);
    JWTDTO getJWT(String email);

}
