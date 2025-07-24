package com.a404.duckonback.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("소셜 로그인 실패: {}", exception.getMessage());

        // 실패 사유를 쿼리 파라미터에 붙여 리디렉션할 수도 있음
        String redirectUrl = "/login/failure?error=" + exception.getMessage();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
