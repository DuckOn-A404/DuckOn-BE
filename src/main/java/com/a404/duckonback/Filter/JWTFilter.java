package com.a404.duckonback.Filter;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.service.UserServiceImpl;
import com.a404.duckonback.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
//    private final JWTTokenService jwtTokenService; // 블랙리스트 검증용
//    private final UserServiceImpl userServiceImpl;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            // 1. JWT 유효성 검사
            if (jwtUtil.validateToken(token)) {

                //2. 블랙리스트 검증 필요 ( Redis 블랙리스트 opr


                // 3. 클레임에서 UUID 추출
                Claims claims = jwtUtil.getClaims(token);
                String userId = claims.getSubject();

                // 4. DB에서 유저 조회
                User user = userRepository.findByUserId(userId);

                if (user != null) {
                    // 5. 인증 객체 생성 및 등록
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        // 6. 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}