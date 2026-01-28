package com.a404.duckonback.filter;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.SocialProvider;
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

        return new CustomUserPrincipal(user, attributes);
    }

    private User upsertUser(OAuth2UserInfo info) {
        // 1) 이메일 우선 매칭(이메일 제공 안 되는 Kakao는 null)
        User user = (info.getEmail() != null)
                ? userRepository.findByEmailAndDeletedFalse(info.getEmail())
                : null;

        // 2) provider + providerId 매칭
        if (user == null) {
            user = userRepository
                    .findByProviderAndProviderIdAndDeletedFalse(info.getProvider(), info.getProviderId())
                    .orElse(null);
        }

        final String computedUserId = buildSocialUserId(info.getProvider(), info.getProviderId());
        final String email = (info.getEmail() != null && !info.getEmail().isBlank())
                ? info.getEmail()
                : generateFakeEmail(info);
        final String nick = (info.getNickname() != null && !info.getNickname().isBlank())
                ? info.getNickname()
                : defaultNickname(info.getProvider());
        final String image = (info.getProfileImage() != null && !info.getProfileImage().isBlank())
                ? info.getProfileImage()
                : null;

        if (user == null) {
            // 신규
            user = User.builder()
                    .email(email)
                    .userId(ensureUserIdNotCollision(computedUserId)) // 유일성 보장
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 랜덤 해시
                    .nickname(nick)
                    .imgUrl(image)
                    .language("ko")
                    .role(UserRole.USER)
                    .provider(info.getProvider())
                    .providerId(info.getProviderId())
                    .hasLocalCredential(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
        } else {
            // 재로그인: 최신 정보 갱신(비어있지 않을 때만)
            // 이메일은 소셜이 바뀌면 위험하므로, 여기선 그대로 두는 게 일반적
            if (image != null && !image.equals(user.getImgUrl())) {
                user.setImgUrl(image);
            }
            if (nick != null && !nick.equals(user.getNickname())) {
                user.setNickname(nick);
            }
            // userId는 한 번 정해지면 고정(이미 로컬/외부에서 사용 중일 수 있음)
            // provider 정보는 최신으로 보정
            user.setProvider(info.getProvider());
            user.setProviderId(info.getProviderId());
            // Dirty Checking으로 업데이트
        }
        return user;
    }

    private String buildSocialUserId(SocialProvider provider, String providerId) {
        String base = provider.name().toLowerCase() + "_" + providerId; // e.g., kakao_12345
        // DB 컬럼 길이(50) 안전 여유
        return base.length() <= 50 ? base : base.substring(0, 50);
    }

    private String ensureUserIdNotCollision(String candidate) {
        // userId 유니크 충돌 시 접미사 부여
        String uid = candidate;
        int seq = 1;
        while (userRepository.findByUserIdAndDeletedFalse(uid) != null) {
            String suffix = "_" + seq++;
            int max = 50 - suffix.length();
            uid = (candidate.length() > max ? candidate.substring(0, max) : candidate) + suffix;
        }
        return uid;
    }

    private String generateFakeEmail(OAuth2UserInfo info) {
        return info.getProvider().name().toLowerCase() + "_" + info.getProviderId() + "@duckon.fake";
    }

    private String defaultNickname(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> "kakaoUser";
            case NAVER -> "naverUser";
            case GOOGLE -> "googleUser";
            default -> "duckonUser";
        };
    }
}
