package com.a404.duckonback.handler;

import com.a404.duckonback.dto.LoginResponseDTO;
import com.a404.duckonback.dto.UserDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.service.ArtistService;
import com.a404.duckonback.service.AuthAuditService;
import com.a404.duckonback.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JsonAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper    objectMapper;
    private final JWTUtil         jwtUtil;
    private final ArtistService   artistService;
    private final AuthAuditService authAuditService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        User user = principal.getUser();

        Instant now = authAuditService.markLoggedIn(user.getId());

        // 토큰 생성
        String access  = jwtUtil.generateAccessToken(user);
        String refresh = jwtUtil.generateRefreshToken(user);

        // DTO 직접 빌드
        UserDTO userDTO = UserDTO.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name())
                .language(user.getLanguage())
                .imgUrl(user.getImgUrl())
                .lastLoginAt(now)
                .artistList(artistService.findAllArtistIdByUserId(user.getId()))
                .build();

        LoginResponseDTO body = LoginResponseDTO.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .user(userDTO)
                .build();

        // JSON 응답
        res.setStatus(HttpStatus.OK.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(res.getWriter(), body);
    }
}

