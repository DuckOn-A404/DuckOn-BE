package com.a404.duckonback.filter;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.UserRole;
import com.a404.duckonback.oauth.userinfo.OAuth2UserInfo;
import com.a404.duckonback.oauth.userinfo.OAuth2UserInfoFactory;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = null;

        // 1) 먼저 userId로 조회
        user = userRepository.findByUserId(username);
        // 2) userId 조회가 안 됐으면 이메일로 시도
        if (user == null) {
            user = userRepository.findByEmail(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        // OAuth2User + UserDetails 를 구현한 CustomUserPrincipal 사용
        return new CustomUserPrincipal(user);
    }

    public static class CustomUserPrincipal implements OAuth2User, UserDetails {
        private final User user;
        private final Map<String,Object> attributes;  // 소셜 로그인 시에만 들어옴

        public CustomUserPrincipal(User user, Map<String,Object> attributes) {
            this.user = user;
            this.attributes = attributes != null ? attributes : Collections.emptyMap();
        }

        // 일반 로그인(JDBC)용 생성자 오버로드
        public CustomUserPrincipal(User user) {
            this(user, Collections.emptyMap());
        }

        public User getUser() { return user; }
        public Long getId()     { return user.getId(); }

        // OAuth2User
        @Override public Map<String,Object> getAttributes()   { return attributes; }
        @Override public String getName()                     { return user.getUserId(); }

        // UserDetails
        @Override public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }
        @Override public String getPassword()    { return user.getPassword(); }
        @Override public String getUsername()    { return user.getUserId(); }
        @Override public boolean isAccountNonExpired()    { return true; }
        @Override public boolean isAccountNonLocked()     { return true; }
        @Override public boolean isCredentialsNonExpired(){ return true; }
        @Override public boolean isEnabled()             { return true; }
    }

    @Service
    @RequiredArgsConstructor
    public static class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(request);

            String registrationId = request.getClientRegistration().getRegistrationId(); // google/kakao/naver
            Map<String, Object> attributes = oAuth2User.getAttributes();

            OAuth2UserInfo info = OAuth2UserInfoFactory.of(registrationId, attributes);

            User user = upsertUser(info);

            return new CustomUserPrincipal(user, attributes); // OAuth2User + UserDetails
        }

        private User upsertUser(OAuth2UserInfo info) {
            // 1) 이메일 기준 우선 탐색 (없을 수도 있음)
            User user = userRepository.findByEmail(info.getEmail());
            if (user == null) {
                // 2) providerId 기준으로 탐색
                user = userRepository.findByProviderAndProviderId(info.getProvider(), info.getProviderId());
            }

            if (user == null) {
                // 신규 가입
                user = User.builder()
                        .email(info.getEmail() != null ? info.getEmail() : generateFakeEmail(info))
                        .userId(generateRandomUserId()) // 외부 공개용 고정 ID
                        .password("oauth")              // 더미값 (NOT NULL 방지)
                        .nickname(info.getNickname())
                        .imgUrl(info.getProfileImage())
                        .language("ko")
                        .role(UserRole.USER)
                        .provider(info.getProvider())
                        .providerId(info.getProviderId())
                        .hasLocalCredential(false)
                        .createdAt(LocalDateTime.now())
                        .build();

                userRepository.save(user);
            } else {
                // 재로그인 시 프로필 업데이트 정도만(선택)
                if (user.getImgUrl() == null && info.getProfileImage() != null) {
                    user.setImgUrl(info.getProfileImage());
                }
                user.setProvider(info.getProvider());
                user.setProviderId(info.getProviderId());
                // userRepository.save(user);  // Dirty checking
            }

            return user;
        }

        private String generateRandomUserId() {
            return "user_" + UUID.randomUUID().toString().substring(0, 8);
        }

        private String generateFakeEmail(OAuth2UserInfo info) {
            return info.getProvider().name().toLowerCase() + "_" + info.getProviderId() + "@duckon.fake";
        }
    }
}