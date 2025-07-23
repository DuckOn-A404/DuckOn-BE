package com.a404.duckonback.service;

import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.entity.User;
import org.springframework.http.ResponseEntity;

public interface UserService {
    User findByEmail(String email);
    User findByUserId(String userId);
    ResponseEntity<?> signup(SignupRequestDTO dto);
}
