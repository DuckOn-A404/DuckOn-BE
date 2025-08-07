package com.a404.duckonback.interceptor;

import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {


        if (request instanceof ServletServerHttpRequest servletRequest) {
            String query = servletRequest.getServletRequest().getQueryString(); // token=...
            String token = null;

            // 1. 쿼리 파라미터에서 토큰 추출
            if (query != null && query.contains("token=")) {
                token = query.replaceFirst(".*token=", "");
                log.debug(">> 쿼리 파라미터 token: {}", token);
            }

            // 2. Authorization 헤더 fallback
            if (token == null) {
                String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    log.debug(">> Authorization 헤더 token: {}", token);
                }
            }

            // 3. 토큰 검증 및 인증 객체 생성
            if (token != null && jwtUtil.validateToken(token)) {
                Authentication authentication = jwtUtil.getAuthentication(token);

                if (authentication != null && authentication.isAuthenticated()) {
                    attributes.put("user", authentication.getPrincipal()); // 인증된 사용자 저장
                    return true;
                } else {
                    log.warn("인증 객체 생성 실패 또는 인증되지 않음");
                }
            } else {
                log.warn("유효하지 않은 JWT 토큰");
            }
        }

        // 인증 실패
        log.warn("WebSocket 핸드셰이크 실패 - 401 Unauthorized");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 후처리 필요시 여기에 작성
    }
}