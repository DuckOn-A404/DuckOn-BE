package com.a404.duckonback.common.config;

import com.a404.duckonback.common.exception.JwtAuthenticationException;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.ApiResponseWriter;
import com.a404.duckonback.common.response.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ApiResponseWriter apiResponseWriter;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 기본: 인증 필요
        ErrorCode code = ErrorCode.USER_NOT_AUTHENTICATED;

        // JWT 커스텀 예외 매핑
        if(authException instanceof JwtAuthenticationException jwtEx){
            String reason = jwtEx.getCode();
            if ("MISSING".equalsIgnoreCase(reason)) {
                code = ErrorCode.MISSING_JWT_TOKEN;
            } else if ("EXPIRED".equalsIgnoreCase(reason)) {
                code = ErrorCode.EXPIRED_JWT_TOKEN;
            } else if ("INVALID".equalsIgnoreCase(reason)) {
                code = ErrorCode.INVALID_JWT_TOKEN;
            } else if ("REVOKED".equalsIgnoreCase(reason)) {
                code = ErrorCode.REVOKED_JWT_TOKEN; // 있으면 추가
            }
        }

        apiResponseWriter.write(response, ApiResponseDTO.fail(code));
    }
}
