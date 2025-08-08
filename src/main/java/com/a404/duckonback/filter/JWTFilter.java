package com.a404.duckonback.filter;

import com.a404.duckonback.config.JwtAuthenticationEntryPoint;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.service.TokenBlacklistService;
import com.a404.duckonback.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    //    private final JWTTokenService jwtTokenService; // 블랙리스트 검증용
//    private final UserServiceImpl userServiceImpl;
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            // 1) JWT 구조/서명 유효성 체크
            if (!jwtUtil.validateToken(token)) {
                entryPoint.commence(request, response,
                        new InsufficientAuthenticationException("유효하지 않은 토큰입니다."));
                return;
            }

            // 2) 블랙리스트에 등록된 토큰인지 체크
            if (blacklistService.isBlacklisted(token)) {
                entryPoint.commence(request, response,
                        new InsufficientAuthenticationException("이미 로그아웃된 토큰입니다."));
                return;
            }
            // denied handler class 구현하기

            // 3) 토큰에서 클레임(poi) 추출 후 Authentication 세팅
            Claims claims = jwtUtil.getClaims(token);
            String userId = claims.getSubject();
            User user = userRepository.findByUserId(userId);

            if (user != null) {
                CustomUserPrincipal principal = new CustomUserPrincipal(user);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        }

        // 6. 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}