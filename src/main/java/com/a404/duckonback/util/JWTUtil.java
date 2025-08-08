// package com.a404.duckonback.util;

// import com.a404.duckonback.config.ServiceProperties;
// import com.a404.duckonback.entity.User;
// import com.a404.duckonback.exception.CustomException;
// import jakarta.annotation.PostConstruct;
// import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Value;

// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Component;

// import java.nio.charset.StandardCharsets;
// import java.security.Key;
// import java.util.Date;
// import java.util.UUID;

// import io.jsonwebtoken.*;
// import io.jsonwebtoken.security.Keys;

// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.Claims;

// import javax.crypto.SecretKey;


// @Component
// @RequiredArgsConstructor
// public class JWTUtil {

//     private final ServiceProperties serviceProperties;
//     private Key key;

//     @PostConstruct
//     public void init() {
//         this.key = Keys.hmacShaKeyFor(
//                 serviceProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8)
//         );
//     }

//     public String generateAccessToken(User user) {
//         return Jwts.builder()
//                 .setSubject(user.getUserId())
//                 .claim("role", user.getRole())
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + serviceProperties.getAccessTokenExpiration()))
//                 .signWith(key)
//                 .compact();
//     }

//     public String generateRefreshToken(User user) {
//         return Jwts.builder()
//                 .setSubject(user.getUserId())
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + serviceProperties.getRefreshTokenExpiration()))
//                 .signWith(key)
//                 .compact();
//     }

//     public boolean validateToken(String token) {
//         try {
//             Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
//             return true;
//         } catch (JwtException e) {
//             return false;
//         }
//     }

//     public Claims getClaims(String token) {
//         return Jwts.parser()
//                 .verifyWith((SecretKey) key)
//                 .build()
//                 .parseSignedClaims(token)
//                 .getPayload();
//     }

//     public void validateTokenFormat(String token) {
//         if (token == null || !token.startsWith("Bearer ")) {
//             throw new CustomException("잘못된 형식의 토큰입니다.", HttpStatus.BAD_REQUEST);
//         }
//         String[] parts = token.split(" ", 2);
//         if (parts.length != 2 || parts[1].isBlank()) {
//             throw new CustomException("잘못된 형식의 토큰입니다.", HttpStatus.BAD_REQUEST);
//         }
//     }

//     public String extractAndValidateToken(String authorization) {
//         validateTokenFormat(authorization);

//         String Token = authorization.substring("Bearer ".length()).trim();
//         if (Token.isEmpty()) {
//             throw new CustomException("Token이 비어 있습니다.", HttpStatus.UNAUTHORIZED);
//         }
//         if (!validateToken(Token)) {
//             throw new CustomException("Token이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
//         }
//         return Token;
//     }


// }
package com.a404.duckonback.util;

import com.a404.duckonback.config.ServiceProperties;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JWTUtil {

    private static final String BEARER_PREFIX = "Bearer ";
    private final ServiceProperties serviceProperties;
    private final UserDetailsService userDetailsService;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                serviceProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 엑세스 토큰 발급
     */
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + serviceProperties.getAccessTokenExpiration()))
                .signWith(key)
                .compact();
    }

    /**
     * 리프레시 토큰 발급
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + serviceProperties.getRefreshTokenExpiration()))
                .signWith(key)
                .compact();
    }

    /**
     * JWT 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * JWT에서 Claims 파싱
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // --- 공용 유틸(중복 제거 핵심) ---
    /** "Bearer xxx" 형식이면 Bearer 제거, 아니면 원문 그대로 반환 (null/공백은 그대로) */
    public String stripBearer(String headerOrToken) {
        if (headerOrToken == null) return null;
        String s = headerOrToken.trim();
        if (s.length() >= BEARER_PREFIX.length()
                && s.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return s.substring(BEARER_PREFIX.length()).trim();
        }
        return s;
    }

    /** Bearer 제거 → 유효성 검사 → 유효하면 토큰 반환, 아니면 null (로그아웃/탈퇴 같은 멱등 처리에 사용) */
    public String normalizeIfValid(String headerOrToken) {
        String token = stripBearer(headerOrToken);
        if (token == null || token.isBlank()) return null;
        return validateToken(token) ? token : null;
    }

    /** 유효하지 않으면 예외. 보호가 필요한 API에서 사용 */
    public String requireValidToken(String headerOrToken) {
        String token = stripBearer(headerOrToken);
        if (token == null || token.isBlank()) {
            throw new CustomException("토큰이 비어 있습니다.", HttpStatus.UNAUTHORIZED);
        }
        if (!validateToken(token)) {
            throw new CustomException("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
        }
        return token;
    }


    /**
     * WebSocket 및 보안 컨텍스트용 인증 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String userId = claims.getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}