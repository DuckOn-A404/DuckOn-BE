package com.a404.duckonback.common.handler;

import com.a404.duckonback.common.util.CookieUtil;
import com.a404.duckonback.domain.auth.dto.LoginResponseDTO;
import com.a404.duckonback.domain.user.dto.UserDTO;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.domain.artist.artist.service.ArtistService;
import com.a404.duckonback.domain.auth.service.AuthAuditService;
import com.a404.duckonback.common.util.JWTUtil;
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

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String SAME_SITE = "Lax";

    private final ObjectMapper    objectMapper;
    private final JWTUtil         jwtUtil;
    private final ArtistService   artistService;
    private final AuthAuditService authAuditService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest req,
            HttpServletResponse res,
            Authentication auth
    ) throws IOException {

        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        User user = principal.getUser();

        Instant now = authAuditService.markLoggedIn(user.getId());

        // 1. 토큰 생성
        String access  = jwtUtil.generateAccessToken(user);
        String refresh = jwtUtil.generateRefreshToken(user);

        // 2. refresh는 HTTP Only 쿠키로 전달
        boolean secure = isHttps(req); // Prod(https) true, Dev, local(http) false
        Integer maxAge = resolveRefreshCookieMaxAge(req); // rememberMe(자동로그인) 여부에 따라 세션/영구 쿠키 결정

        CookieUtil.setHttpOnlyCookie(
                res,
                REFRESH_COOKIE_NAME,
                refresh,
                secure,
                SAME_SITE,
                maxAge
        );

        // 3. 응답 body에는 access 토큰과 유저 정보만 담아서 JSON으로 전달
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
                .user(userDTO)
                .build();

        // 4. JSON 응답
        res.setStatus(HttpStatus.OK.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(res.getWriter(), body);
    }

    /**
     * 현재 요청이 https인지 판단 (프록시 뒤면 X-Forwarded-Proto까지 확인)
     */
    private boolean isHttps(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null) {
            return "https".equalsIgnoreCase(forwardedProto);
        }
        return request.isSecure();
    }

    /**
     * 자동로그인(rememberMe) ON/OFF에 따라 refresh 쿠키 만료를 결정
     * - ON  : persistent cookie (예: 14일)
     * - OFF : session cookie (null)
     */
    private Integer resolveRefreshCookieMaxAge(HttpServletRequest request) {
        Object v = request.getAttribute("rememberMe");
        boolean rememberMe = (v instanceof Boolean b) && b;

        if (rememberMe) {
            return 14 * 24 * 60 * 60; // 14일
        }
        return null; // 세션 쿠키
    }
}

