package com.a404.duckonback.config;

import com.a404.duckonback.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import com.a404.duckonback.filter.CustomOAuth2UserService;
import com.a404.duckonback.filter.JWTFilter;
import com.a404.duckonback.handler.*;
import com.a404.duckonback.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    //    private final CustomUserDetailsService userDetailsService; // 자동 주입되어서 사용하지 않음
    private final JWTFilter jwtFilter;
    private final JsonAuthSuccessHandler    jsonSuccessHandler;
    private final JsonAuthFailureHandler jsonFailureHandler;
    private final OAuth2AuthSuccessHandler  oauth2SuccessHandler;
    private final OAuth2AuthFailureHandler  oauth2FailureHandler;
    private final JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    // CORS 허용 Origin 상수
    private static final List<String> CORS_ALLOWED_ORIGINS = List.of(
            "https://memeduck.site",
            "https://www.memeduck.site",
            "https://duckon.site",
            "https://www.duckon.site",
            "https://d3jianh0vyc8he.cloudfront.net",
            "http://ec2-43-202-159-100.ap-northeast-2.compute.amazonaws.com",
            "http://127.0.0.1:3000",
            "http://localhost:3000",
            "http://localhost:5173"
    );

    private static final List<String> CORS_ALLOWED_METHODS= List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    );

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(CORS_ALLOWED_ORIGINS);
        config.setAllowedMethods(CORS_ALLOWED_METHODS);
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomOAuth2UserService oauth2UserService,
                                           AuthenticationManager authManager) throws Exception {
        // JSON 로그인 필터
        CustomJsonUsernamePasswordAuthenticationFilter jsonFilter =
                new CustomJsonUsernamePasswordAuthenticationFilter(authManager);

        jsonFilter.setFilterProcessesUrl("/api/auth/login");
        jsonFilter.setAuthenticationSuccessHandler(jsonSuccessHandler);
        jsonFilter.setAuthenticationFailureHandler(jsonFailureHandler);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 0. 예외적으로 인증이 필요한 endpoint
                        .requestMatchers(SecurityEndpoints.AUTH_REQUIRED).authenticated()
                        // 같은 경로의 get은 허용, post만 비허용
                        .requestMatchers(HttpMethod.POST, "/api/chat/artist/**").authenticated()

                        // 1. Swagger, 문서
                        .requestMatchers(SecurityEndpoints.SWAGGER).permitAll()

                        // 2. WebSocket
                        .requestMatchers(SecurityEndpoints.WS).permitAll()

                        // 3. 메소드 상관 없이 전체 허용
                        .requestMatchers(SecurityEndpoints.PUBLIC_ANY).permitAll()

                        // 4. GET/POST 메소드
                        .requestMatchers(HttpMethod.GET, SecurityEndpoints.PUBLIC_GET).permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityEndpoints.PUBLIC_POST).permitAll()

                        // 5. ADMIN
                        .requestMatchers(SecurityEndpoints.ADMIN).hasRole("ADMIN")

                        // 6. OAuth / Auth 기본
                        .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()

                        // 7. 이 외 모두 기본 인증 필요
                        .anyRequest().authenticated()
                )

                // 기존 formLogin 대신 jsonFilter 사용
                .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class)
                // 2) OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(ui -> ui.userService(oauth2UserService))
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                )// JWT 검사 필터
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)// 3. 예외 처리: 인증/인가 실패 시 진입점 지정
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)     // 유효하지 않은 토큰 등
                        .accessDeniedHandler(accessDeniedHandler)               // 권한 부족
                );

        return http.build();
    }
}
