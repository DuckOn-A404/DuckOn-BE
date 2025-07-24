package com.a404.duckonback.util;

import com.a404.duckonback.entity.User;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import javax.crypto.SecretKey;


@Component
public class JWTUtil {
    // (임시) 임의 키로 토큰 생성
    private final Key key = Keys.hmacShaKeyFor("DuckOnSecretKeyMustBeAtLeast32ByteLong".getBytes(StandardCharsets.UTF_8));

//    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // ( 임시 ) 서버 실행마다 바뀜 -> 기존 토큰 무효화됨
    private final long accessTokenExpiration = 1000L * 60 * 60 * 24 * 30; // ( 임시 ) 30일
    private final long refreshTokenExpiration = 1000L * 60 * 60 * 24 * 30; // ( 임시 ) 30일

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
