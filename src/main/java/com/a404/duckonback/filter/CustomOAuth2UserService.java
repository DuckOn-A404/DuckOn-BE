package com.a404.duckonback.filter;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.UserRole;
import com.a404.duckonback.oauth.userinfo.OAuth2UserInfo;
import com.a404.duckonback.oauth.userinfo.OAuth2UserInfoFactory;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

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