package com.a404.duckonback.oauth.handler;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        // TODO: refreshToken 저장/회전 로직(Redis/DB)

        // 쿠키로 내려주거나, 프론트 리다이렉트 URL에 심볼릭 코드만 붙인 뒤 프론트가 토큰 요청하도록 유도
        CookieUtil.addHttpOnlyCookie(response, "access_token", accessToken, 900);
        CookieUtil.addHttpOnlyCookie(response, "refresh_token", refreshToken, 1209600);

        // 최초 로그인 & hasLocalCredential=false 이면 ID/PW 설정 페이지로
        String target = user.getHasLocalCredential() != null && !user.getHasLocalCredential()
                ? "/onboarding/set-credential"
                : "/login/success";

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
