package com.a404.duckonback.handler;

import com.a404.duckonback.config.ServiceProperties;
import com.a404.duckonback.dto.LoginResponseDTO;
import com.a404.duckonback.dto.UserDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.service.ArtistService;
import com.a404.duckonback.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final ArtistService artistService;
    private final ObjectMapper objectMapper;
    private final ServiceProperties serviceProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 1) Principal 에서 User 꺼내기
        User user;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserPrincipal cu) {
            user = cu.getUser();
        } else if (principal instanceof UserDetails ud) {
            user = userRepository.findByUserId(ud.getUsername());
        } else {
            throw new IllegalStateException("Unknown principal: " + principal);
        }

        // 2) JWT 생성
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        // TODO: refreshToken 저장/회전 로직

        // 3) 성공 리다이렉트 URI 조합
        String redirectUri = UriComponentsBuilder
                .fromUriString(serviceProperties.getOauth2SuccessUrl())
                .queryParam("accessToken",  accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        // 4) 리다이렉트 (React 쪽 /oauth2/success 로 넘어감)
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.sendRedirect(redirectUri);
    }

}
