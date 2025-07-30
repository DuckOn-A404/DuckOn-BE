package com.a404.duckonback.handler;

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

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final ArtistService artistService;
    private final ObjectMapper objectMapper;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // principal에는 CustomUserPrincipal (OAuth2) 또는 UserDetails (폼 로그인)가 담겨 있음
        User user;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserPrincipal cu) {
            user = cu.getUser();
        } else if (principal instanceof UserDetails ud) {
            user = userRepository.findByUserId(ud.getUsername());
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal);
        }

        // 1) 토큰 생성
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        // TODO: refreshToken 저장/회전 로직(Redis/DB)

//        // 쿠키로 내려주거나, 프론트 리다이렉트 URL에 심볼릭 코드만 붙인 뒤 프론트가 토큰 요청하도록 유도
//        CookieUtil.addHttpOnlyCookie(response, "access_token", accessToken, 900);
//        CookieUtil.addHttpOnlyCookie(response, "refresh_token", refreshToken, 1209600);

        //  2) 사용자 정보를 DTO로 변환
        UserDTO userDTO = UserDTO.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name())
                .language(user.getLanguage())
                .imgUrl(user.getImgUrl())
                .artistList(artistService.findAllArtistIdByUserId(user.getId()))
                .build();

        // 사용자 정보와 토큰을 포함한 응답 DTO 생성
        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();

        // 3) JSON 반환
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), responseDTO);
    }
}
