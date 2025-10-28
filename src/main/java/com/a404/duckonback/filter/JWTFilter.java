package com.a404.duckonback.filter;

import com.a404.duckonback.config.JWTAuthenticationEntryPoint;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.TokenStatus;
import com.a404.duckonback.exception.JwtAuthenticationException;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.service.TokenBlacklistService;
import com.a404.duckonback.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    //    private final JWTTokenService jwtTokenService; // 블랙리스트 검증용
//    private final UserServiceImpl userServiceImpl;
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;
    private final JWTAuthenticationEntryPoint entryPoint;

//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7).trim();
//
//            // 1) JWT 구조/서명 유효성 체크
//            if (!jwtUtil.validateToken(token)) {
//                entryPoint.commence(request, response,
//                        new InsufficientAuthenticationException("유효하지 않은 토큰입니다."));
//                return;
//            }
//
//            // 2) 블랙리스트에 등록된 토큰인지 체크
//            if (blacklistService.isBlacklisted(token)) {
//                entryPoint.commence(request, response,
//                        new InsufficientAuthenticationException("이미 로그아웃된 토큰입니다."));
//                return;
//            }
//            // denied handler class 구현하기
//
//            // 3) 토큰에서 클레임(poi) 추출 후 Authentication 세팅
//            Claims claims = jwtUtil.getClaims(token);
//            String userId = claims.getSubject();
//            User user = userRepository.findByUserIdAndDeletedFalse(userId);
//
//            if (user != null) {
//                com.a404.duckonback.filter.CustomUserPrincipal principal = new com.a404.duckonback.filter.CustomUserPrincipal(user);
//                UsernamePasswordAuthenticationToken auth =
//                        new UsernamePasswordAuthenticationToken(
//                                principal, null, principal.getAuthorities()
//                        );
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            }
//
//        }
//
//        // 6. 다음 필터로 이동
//        filterChain.doFilter(request, response);
//    }

    // ★★★ 화이트리스트: JWT 검증을 완전히 건너뛸 경로들
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> WHITELIST = List.of(
            "/api/auth/**",          // 로그인/리프레시/회원가입 등
            "/oauth2/**",            // OAuth2 플로우
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**",
            "/ws-chat/**"            // (필요시) 웹소켓 핸드셰이크 경로
    );

    /**
     * ★ 이 요청은 JWTFilter를 적용하지 않는다.
     *    - 화이트리스트 매칭
     *    - 혹은 CORS preflight(OPTIONS)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 프리플라이트 제외 (원치 않으면 제거 가능)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        for (String pattern : WHITELIST) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true; // ← 이 경우 doFilterInternal 자체가 실행되지 않음
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            // 1) JWT 상태 확인 (EXPIRED, INVALID, VALID 등)
            TokenStatus status = jwtUtil.getTokenValidationStatus(token);

            if (status == TokenStatus.EXPIRED) {
                entryPoint.commence(request, response,
                        new JwtAuthenticationException("토큰이 만료되었습니다.", "EXPIRED"));
                return;
            }

            if (status == TokenStatus.INVALID) {
                entryPoint.commence(request, response,
                        new JwtAuthenticationException("유효하지 않은 토큰입니다.", "INVALID"));
                return;
            }

            // 2) 블랙리스트 확인
            if (blacklistService.isBlacklisted(token)) {
                entryPoint.commence(request, response,
                        new JwtAuthenticationException("이미 로그아웃된 토큰입니다.", "REVOKED"));
                return;
            }

            // 3) 토큰에서 유저 정보 추출 및 인증 객체 세팅
            Claims claims = jwtUtil.getClaims(token);
            String userId = claims.getSubject();
            User user = userRepository.findByUserIdAndDeletedFalse(userId);

            if (user != null) {
                CustomUserPrincipal principal = new CustomUserPrincipal(user);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // 4. 다음 필터로 이동
        filterChain.doFilter(request, response);
    }

}