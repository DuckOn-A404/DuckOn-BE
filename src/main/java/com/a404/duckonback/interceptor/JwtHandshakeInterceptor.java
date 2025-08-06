package com.a404.duckonback.interceptor;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request,
//                                   ServerHttpResponse response,
//                                   WebSocketHandler wsHandler,
//                                   Map<String, Object> attributes) throws Exception {
//        if (request instanceof ServletServerHttpRequest servletRequest) {
//            String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//                if (jwtUtil.validateToken(token)) {
//                    String userId = jwtUtil.getClaims(token).getSubject();
//                    User user = userRepository.findByUserId(userId);
//                    if (user != null) {
//                        attributes.put("user", user); // 이후 WebSocket 메시지 처리 시 사용 가능
//                        return true;
//                    }
//                }
//            }
//        }
//        response.setStatusCode(HttpStatus.UNAUTHORIZED);
//        return false;
//    }

@Override
public boolean beforeHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Map<String, Object> attributes) throws Exception {
    log.debug(">> [WebSocket] Handshake 시작");

    if (request instanceof ServletServerHttpRequest servletRequest) {
        String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
        log.debug(">> Authorization 헤더: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug(">> JWT 추출: {}", token);

            boolean isValid = jwtUtil.validateToken(token);
            log.debug(">> JWT 유효성: {}", isValid);

            if (isValid) {
                String userId = jwtUtil.getClaims(token).getSubject();
                log.debug(">> JWT에서 추출한 userId: {}", userId);

                User user = userRepository.findByUserId(userId);
                if (user != null) {
                    attributes.put("user", user);
                    log.info("✅ WebSocket 사용자 인증 성공 - userId: {}", user.getUserId());
                    return true;
                } else {
                    log.warn("❌ JWT는 유효하지만 userId '{}'에 해당하는 사용자를 찾을 수 없습니다", userId);
                }
            } else {
                log.warn("❌ JWT 유효성 검증 실패");
            }
        } else {
            log.warn("❌ Authorization 헤더가 없거나 Bearer 토큰이 아닙니다");
        }
    }

    log.warn("❌ WebSocket 핸드셰이크 실패 - 401 Unauthorized");
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return false;
}

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        //...
    }


}
