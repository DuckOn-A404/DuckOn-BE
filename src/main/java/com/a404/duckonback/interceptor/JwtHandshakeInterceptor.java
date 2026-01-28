package com.a404.duckonback.interceptor;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.TokenStatus;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.service.UserService;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            var http = servletRequest.getServletRequest();

            // 1) token 안전 추출 (query → header 순)
            String token = null;

            String query = http.getQueryString();
            if (query != null) {
                for (String part : query.split("&")) {
                    if (part.startsWith("token=")) {
                        token = java.net.URLDecoder.decode(part.substring(6), java.nio.charset.StandardCharsets.UTF_8);
                        break;
                    }
                }
            }
            if (token == null) {
                String authHeader = http.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7).trim();
                }
            }

            // 2) 토큰 없음 → 게스트 허용
            if (token == null || token.isBlank()) {
                String sid = http.getSession(true).getId();
                String guestId = "guest:" + sid;
                String guestNick = "익명의 사용자";

                attributes.put("guest", Boolean.TRUE);
                attributes.put("guestId", guestId);
                attributes.put("guestNickname", guestNick);
                return true;
            }

            // 3) 토큰 있음 -> 상태 확인
            TokenStatus status = jwtUtil.getTokenValidationStatus(token);
            if (status == TokenStatus.VALID) {
                var auth = jwtUtil.getAuthentication(token);
                if (auth != null && auth.isAuthenticated()) {
                    Object principal = auth.getPrincipal();
                    User user = null;
                    if (principal instanceof User u) {
                        user = u;
                    } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                        user = userRepository.findByUserIdAndDeletedFalse(ud.getUsername());
                    }
                    if (user != null) {
                        attributes.put("user", user);
                        return true;
                    }
                }
                // 인증객체 만들지 못하면 게스트로
                attributes.put("guest", Boolean.TRUE);
                return true;
            }

            // 4) INVALID/EXPIRED -> 차단하지 말고 게스트로 허용
            attributes.put("guest", Boolean.TRUE);
            return true;
        }

        // 비정상 요청만 차단
        response.setStatusCode(HttpStatus.BAD_REQUEST);
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