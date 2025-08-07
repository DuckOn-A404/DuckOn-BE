package com.a404.duckonback.handler;

import com.a404.duckonback.config.ServiceProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthFailureHandler implements AuthenticationFailureHandler {
    private final ServiceProperties props;

    @Override
    public void onAuthenticationFailure(HttpServletRequest req,
                                        HttpServletResponse res,
                                        AuthenticationException ex) throws IOException {
        // 프론트 쪽 에러 페이지로 리다이렉트
        String target = UriComponentsBuilder
                .fromUriString(props.getOauth2FailureUrl())   // ex: https://my.front/app/oauth2/failure
                .queryParam("error", URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8))
                .build().toUriString();
        res.setStatus(HttpStatus.FOUND.value());
        res.sendRedirect(target);
    }
}
