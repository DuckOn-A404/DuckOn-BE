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
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "https://memeduck.site",
                "https://www.memeduck.site",
                "https://duckon.site",
                "https://www.duckon.site",
                "https://d3jjanh0vyc8he.cloudfront.net",
                "http://ec2-43-202-159-100.ap-northeast-2.compute.amazonaws.com",
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(List.of("*"));        // 모든 Method 허용
        configuration.setAllowedHeaders(List.of("*"));        // 모든 Header 허용
        configuration.setAllowCredentials(true);              // 쿠키/인증정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomOAuth2UserService oauth2UserService,
                                           AuthenticationManager authManager, TokenBlacklistService tokenBlacklistService) throws Exception {
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
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**",
                                        "/webjars/**",
                                        "/api/rooms/{roomId}/enter",
                                        "/ws-chat/**"
                                ).permitAll()

                                // 1) 인증 필요 API (특정 /me, /follow, PUT /follow)
                                .requestMatchers("/api/artists/me").authenticated()
                                .requestMatchers(HttpMethod.POST,   "/api/artists/*/follow").authenticated() // 팔로우
                                .requestMatchers(HttpMethod.DELETE, "/api/artists/*/follow").authenticated() // 팔로우/언팔로우
                                .requestMatchers(HttpMethod.PUT,    "/api/artists/follow").authenticated() // 팔로우/언팔로우 토글
                                .requestMatchers(HttpMethod.POST, "/api/chat/artist/**").authenticated() // 채팅 메시지 전송
                                // 2) 누구나 볼 수 있는 조회 API
                                .requestMatchers(HttpMethod.GET, "/api/artists").permitAll()           // 페이징 조회 & 키워드
                                .requestMatchers(HttpMethod.GET, "/api/artists/random").permitAll()    // 랜덤
                                .requestMatchers(HttpMethod.GET, "/api/artists/*").permitAll()         // 단일 상세
                                .requestMatchers(HttpMethod.GET, "/api/chat/artist/**").permitAll() // 채팅 내역 조회
                                .requestMatchers(HttpMethod.POST, "/api/rooms/*/enter").permitAll()
                                .requestMatchers(HttpMethod.POST,"/api/rooms/*/exit").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/rooms").permitAll() // 방 목록 조회
                                .requestMatchers(HttpMethod.GET, "/api/rooms/*").permitAll() // 방 상세 조회 // 막아야하지만 프론트가 처리했다고 막지 말라고 요청하심
                                .requestMatchers(HttpMethod.GET, "/api/rooms/trending/*").permitAll() // 트렌딩 방 조회
                                .requestMatchers(HttpMethod.GET, "/api/users/recommendations").permitAll() // 추천 유저 조회
                                .requestMatchers(HttpMethod.GET, "/api/public/youtube/meta/*").permitAll() // 유튜브 메타데이터 조회

                                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin API
                                // Auth API
                                .requestMatchers("/api/auth/logout").authenticated()
                                .requestMatchers("/api/auth/**", "/oauth2/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

//                        .requestMatchers("/").hasAnyRole("USER", "ADMIN")
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
