package com.a404.duckonback.config;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import com.a404.duckonback.filter.CustomUserDetails;
import com.a404.duckonback.filter.CustomUserDetailsService;
import com.a404.duckonback.filter.JWTFilter;
import com.a404.duckonback.handler.AuthFailureHandler;
import com.a404.duckonback.handler.AuthSuccessHandler;
import com.a404.duckonback.oauth.service.CustomOAuth2UserService;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomOAuth2UserService oauth2UserService,
                                           AuthenticationManager authManager) throws Exception {
        // JSON 로그인 필터에도 AuthenticationManager 주입
        CustomJsonUsernamePasswordAuthenticationFilter jsonFilter =
                new CustomJsonUsernamePasswordAuthenticationFilter(authManager);
        jsonFilter.setAuthenticationSuccessHandler(successHandler);
        jsonFilter.setAuthenticationFailureHandler(failureHandler);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/auth/logout").authenticated()
                                .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
//                        .requestMatchers("/").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                // 폼 로그인 비활성화
//                // 1) 폼 로그인도 같은 핸들러 사용
//                .formLogin(form -> form
//                        .loginProcessingUrl("/api/auth/login")
//                        .successHandler(successHandler)
//                        .failureHandler(failureHandler)
//                        .permitAll()
//                )
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
