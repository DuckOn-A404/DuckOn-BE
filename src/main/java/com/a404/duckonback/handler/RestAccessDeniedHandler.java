package com.a404.duckonback.handler;

import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.ApiResponseWriter;
import com.a404.duckonback.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 되었으나 권한이 부족한 사용자가 접근할 때 처리하는 핸들러
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ApiResponseWriter apiResponseWriter;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ApiResponseDTO<?> body = ApiResponseDTO.fail(ErrorCode.FORBIDDEN);
        apiResponseWriter.write(response, body);
    }
}
