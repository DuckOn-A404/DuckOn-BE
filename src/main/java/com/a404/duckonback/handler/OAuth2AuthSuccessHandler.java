package com.a404.duckonback.handler;

import com.a404.duckonback.config.ServiceProperties;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.filter.CustomUserDetailsService;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.service.ArtistService;
import com.a404.duckonback.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthSuccessHandler implements AuthenticationSuccessHandler {
    private final JWTUtil         jwtUtil;
    private final UserRepository  userRepository;
    private final ArtistService   artistService;
    private final ServiceProperties serviceProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        // OAuth2User (CustomUserPrincipal) 또는 UserDetails 에서 User 조회
        User user;
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetailsService.CustomUserPrincipal cu) {
            user = cu.getUser();
        } else {
            String username = ((UserDetails) principal).getUsername();
            user = userRepository.findByUserId(username);
        }

        // 토큰 생성
        String access  = jwtUtil.generateAccessToken(user);
        String refresh = jwtUtil.generateRefreshToken(user);

        // 빌더를 통해 redirect URI 조합
        String redirectUri = UriComponentsBuilder
                .fromUriString(serviceProperties.getOauth2SuccessUrl())
                .queryParam("accessToken", access)
                .queryParam("refreshToken", refresh)
                .build()
                .toUriString();

        // 리다이렉트
        res.setStatus(HttpStatus.FOUND.value());
        res.sendRedirect(redirectUri);
    }
}

