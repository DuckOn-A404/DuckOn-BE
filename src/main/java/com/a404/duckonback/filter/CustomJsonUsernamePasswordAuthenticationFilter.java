package com.a404.duckonback.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class CustomJsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomJsonUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/api/auth/login"); // login URL
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // JSON 요청 바디 파싱
            Map<String, String> requestMap = objectMapper.readValue(request.getInputStream(), Map.class);

            String email = requestMap.get("email");
            String userId = requestMap.get("userId");
            String password = requestMap.get("password");

            String principal = (email != null && !email.isBlank()) ? email : userId;

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(principal, password);

            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            log.error("JSON 로그인 요청 파싱 오류", e);
            throw new RuntimeException(e);
        }
    }
}
