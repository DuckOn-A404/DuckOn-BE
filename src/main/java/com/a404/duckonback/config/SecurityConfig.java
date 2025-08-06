package com.a404.duckonback.config;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import com.a404.duckonback.filter.CustomUserDetailsService;
import com.a404.duckonback.filter.JWTFilter;
import com.a404.duckonback.handler.AuthFailureHandler;
import com.a404.duckonback.handler.AuthSuccessHandler;
import com.a404.duckonback.oauth.service.CustomOAuth2UserService;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JWTFilter jwtFilter;
    private final AuthSuccessHandler successHandler;
    private final AuthFailureHandler failureHandler;

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
        configuration.setAllowedOriginPatterns(List.of("*")); // 모든 Origin 허용 (임시)
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
                                           AuthenticationManager authManager) throws Exception {
        // JSON 로그인 필터에도 AuthenticationManager 주입
        CustomJsonUsernamePasswordAuthenticationFilter jsonFilter =
                new CustomJsonUsernamePasswordAuthenticationFilter(authManager);
        jsonFilter.setAuthenticationSuccessHandler(successHandler);
        jsonFilter.setAuthenticationFailureHandler(failureHandler);


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
                                        "/api/rooms/{roomId}/enter"
//                                        "/ws-chat/**"
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
                                .requestMatchers(HttpMethod.GET, "/api/rooms").permitAll() // 방 목록 조회
                                .requestMatchers(HttpMethod.GET, "/api/rooms/*").permitAll() // 방 상세 조회

                                // Auth API
                                .requestMatchers("/api/auth/logout").authenticated()
                                .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()

//                        .requestMatchers("/").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )

                // 기존 formLogin 대신 jsonFilter 사용
                .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class)
                // 2) OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(ui -> ui.userService(oauth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                );

        // 3) JWT 검사 필터
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
