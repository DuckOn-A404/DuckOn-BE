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
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;
    private final JWTAuthenticationEntryPoint entryPoint;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // 게스트 허용 경로 (필터는 타되, 토큰 없거나/만료/무효여도 통과시킬 경로)
    private static final List<String> GUEST_ALLOWED = List.of(
            "/api/rooms/*/enter",
            "/api/rooms/*/exit"
    );

    // 완전 스킵(필터 미적용) 경로
    private static final List<String> WHITELIST = List.of(
            "/api/auth/**",
            "/login/oauth2/**",
            "/oauth2/**",
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**",
            "/ws-chat/**"
    );

    private static final List<String> BLACKLIST = List.of(
            "/api/auth/logout"
    );

    private boolean matchesAny(String uri, List<String> patterns) {
        for (String p : patterns) if (PATH_MATCHER.match(p, uri)) return true;
        return false;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String uri = request.getRequestURI();
        if (matchesAny(uri, BLACKLIST)) return false;
        return matchesAny(uri, WHITELIST);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // 1) 토큰 없음
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 게스트 허용 경로면 그대로 통과 (게스트)
            if (matchesAny(uri, GUEST_ALLOWED)) {
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }
            // 그 외는 SecurityConfig 인가 규칙에 맡김 (permitAll이면 통과, authenticated면 401)
            chain.doFilter(request, response);
            return;
        }

        // 2) 토큰 있음 → 상태 평가
        String token = authHeader.substring(7).trim();
        TokenStatus status = jwtUtil.getTokenValidationStatus(token);
        boolean revoked = blacklistService.isBlacklisted(token);
        boolean bad = (status == TokenStatus.EXPIRED) || (status == TokenStatus.INVALID) || revoked;

        if (bad) {
            // 게스트 허용 경로면 게스트로 통과
            if (matchesAny(uri, GUEST_ALLOWED)) {
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }
            // 기본: 401
            String reason = (status == TokenStatus.EXPIRED) ? "EXPIRED" : (revoked ? "REVOKED" : "INVALID");
            entryPoint.commence(request, response,
                    new JwtAuthenticationException("유효하지 않은 토큰입니다.", reason));
            return;
        }

        // 3) VALID → 인증 세팅
        Claims claims = jwtUtil.getClaims(token);
        String userId = claims.getSubject();
        User user = userRepository.findByUserIdAndDeletedFalse(userId);
        if (user != null) {
            CustomUserPrincipal principal = new CustomUserPrincipal(user);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
