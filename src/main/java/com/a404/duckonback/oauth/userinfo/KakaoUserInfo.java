package com.a404.duckonback.oauth.userinfo;

import com.a404.duckonback.enums.SocialProvider;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    public KakaoUserInfo(Map<String, Object> attributes){ this.attributes = attributes; }

    @Override public String getProviderId() { return String.valueOf(attributes.get("id")); }
    @Override public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }
    @Override public String getNickname() {
        Map<String, Object> profile = (Map<String, Object>) ((Map)attributes.get("kakao_account")).get("profile");
        return profile != null ? (String) profile.get("nickname") : "kakaoUser";
    }
    @Override public String getProfileImage() {
        Map<String, Object> profile = (Map<String, Object>) ((Map)attributes.get("kakao_account")).get("profile");
        return profile != null ? (String) profile.get("profile_image_url") : null;
    }
    @Override public SocialProvider getProvider() { return SocialProvider.KAKAO; }
}

