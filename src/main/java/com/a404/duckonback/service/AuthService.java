package com.a404.duckonback.service;

import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequest);

}
