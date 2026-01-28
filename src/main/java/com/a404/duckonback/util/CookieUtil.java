package com.a404.duckonback.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

public class CookieUtil {

    /**
     * HttpOnly 쿠키 추가
     */
    public static void addHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);    // JavaScript에서 접근 불가
        cookie.setSecure(true);      // HTTPS에서만 전송 (개발 환경에서는 false 가능)
        cookie.setMaxAge(maxAgeInSeconds);
        response.addCookie(cookie);
    }

    /**
     * 쿠키 삭제 (같은 이름의 빈값 쿠키 추가)
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);         // 0초 = 즉시 만료
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    /**
     * 쿠키 조회
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 쿠키 값 조회 (null-safe)
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie cookie = getCookie(request, name);
        return cookie != null ? cookie.getValue() : null;
    }
}
