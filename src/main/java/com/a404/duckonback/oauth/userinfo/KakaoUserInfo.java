package com.a404.duckonback.oauth.userinfo;

import com.a404.duckonback.enums.SocialProvider;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    public KakaoUserInfo(Map<String, Object> attributes){ this.attributes = attributes; }

    @Override public String getProviderId() {
        Object id = attributes.get("id");
        return id != null ? String.valueOf(id) : null;
    }

    @Override public String getEmail() {
        Map<String, Object> kakaoAccount = safeMap(attributes.get("kakao_account"));
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    @Override public String getNickname() {
        Map<String, Object> kakaoAccount = safeMap(attributes.get("kakao_account"));
        Map<String, Object> profile = kakaoAccount != null ? safeMap(kakaoAccount.get("profile")) : null;
        String nickname = profile != null ? (String) profile.get("nickname") : null;
        return nickname != null && !nickname.isBlank() ? nickname : "kakaoUser";
    }

    @Override public String getProfileImage() {
        Map<String, Object> kakaoAccount = safeMap(attributes.get("kakao_account"));
        Map<String, Object> profile = kakaoAccount != null ? safeMap(kakaoAccount.get("profile")) : null;
        String url = profile != null ? (String) profile.get("profile_image_url") : null;
        return (url != null && !url.isBlank()) ? url : null;
    }

    @Override public SocialProvider getProvider() { return SocialProvider.KAKAO; }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Object obj) {
        return (obj instanceof Map) ? (Map<String, Object>) obj : null;
    }
}
