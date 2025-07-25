package com.a404.duckonback.oauth.userinfo;

import com.a404.duckonback.enums.SocialProvider;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    public NaverUserInfo(Map<String, Object> attributes){ this.attributes = attributes; }

    @Override public String getProviderId() {
        Map<String,Object> res = (Map<String,Object>) attributes.get("response");
        return (String) res.get("id");
    }
    @Override public String getEmail() {
        Map<String,Object> res = (Map<String,Object>) attributes.get("response");
        return (String) res.get("email");
    }
    @Override public String getNickname() {
        Map<String,Object> res = (Map<String,Object>) attributes.get("response");
        return (String) res.getOrDefault("nickname", "naverUser");
    }
    @Override public String getProfileImage() {
        Map<String,Object> res = (Map<String,Object>) attributes.get("response");
        return (String) res.get("profile_image");
    }
    @Override public SocialProvider getProvider() { return SocialProvider.NAVER; }
}
