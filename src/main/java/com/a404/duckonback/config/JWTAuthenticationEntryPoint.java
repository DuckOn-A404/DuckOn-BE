package com.a404.duckonback.config;

import com.a404.duckonback.exception.JwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 기본 값
        String code = "UNAUTHORIZED";
        String message = authException.getMessage();

        // JwtAuthenticationException일 경우, code 추출
        if (authException instanceof JwtAuthenticationException jwtEx) {
            code = jwtEx.getCode();       // 예: "EXPIRED", "INVALID"
            message = jwtEx.getMessage(); // 예: "토큰이 만료되었습니다."
        }

        Map<String, Object> body = Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", message,
                "code", code
        );

        mapper.writeValue(response.getOutputStream(), body);
    }
}
