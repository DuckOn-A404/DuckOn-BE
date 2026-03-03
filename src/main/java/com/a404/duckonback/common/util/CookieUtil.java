package com.a404.duckonback.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

public class CookieUtil {

    private CookieUtil(){}

    /**
     * HttpOnly 쿠키 추가
     */
    public static void setHttpOnlyCookie(
            HttpServletResponse response,
            String name,
            String value,
            boolean secure,
            String sameSite,
            Integer maxAgeSec
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value).append("; Path=/; HttpOnly; ");

        if(secure) sb.append("Secure; ");
        if(sameSite!=null) sb.append("SameSite=").append(sameSite).append("; ");
        if(maxAgeSec != null) sb.append("Max-Age=").append(maxAgeSec).append("; ");

        response.addHeader("Set-Cookie", sb.toString());
    }

    /**
     * 쿠키 삭제 (같은 이름의 빈값 쿠키 추가)
     */
    public static void deleteCookie(
            HttpServletResponse response,
            String name,
            boolean secure,
            String sameSite
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=; Path=/; HttpOnly; Max-Age=0; ");

        if(secure) sb.append("Secure; ");
        if(sameSite!=null) sb.append("SameSite=").append(sameSite).append("; ");

        response.addHeader("Set-Cookie", sb.toString());
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
