package com.a404.duckonback.oauth.userinfo;

import com.a404.duckonback.enums.SocialProvider;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    public NaverUserInfo(Map<String, Object> attributes){ this.attributes = attributes; }

    @Override public String getProviderId() {
        Map<String,Object> res = (Map<String,Object>) attributes.get("response");
        return res != null ? (String) res.get("id") : null;
    }
    @Override public String getEmail() {
        Map<String,Object> res = (Map<String,Object>) attributes.get("response");
        return res != null ? (String) res.get("email") : null;
    }
    @Override public String getNickname() {
        Map<String,Object> res = (Map<String,Object>) attributes.get("response");
        String nick = res != null ? (String) res.get("nickname") : null;
        return (nick != null && !nick.isBlank()) ? nick : "naverUser";
    }
    @Override public String getProfileImage() {
        Map<String,Object> res = (Map<String,Object>) attributes.get("response");
        String url = res != null ? (String) res.get("profile_image") : null;
        return (url != null && !url.isBlank()) ? url : null;
    }
    @Override public SocialProvider getProvider() { return SocialProvider.NAVER; }
}

