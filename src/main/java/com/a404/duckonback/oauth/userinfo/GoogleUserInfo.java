package com.a404.duckonback.oauth.userinfo;

import com.a404.duckonback.enums.SocialProvider;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    public GoogleUserInfo(Map<String, Object> attributes){ this.attributes = attributes; }

    @Override public String getProviderId() { return (String) attributes.get("sub"); }
    @Override public String getEmail() { return (String) attributes.get("email"); }
    @Override public String getNickname() {
        String name = (String) attributes.getOrDefault("name", "googleUser");
        return (name != null && !name.isBlank()) ? name : "googleUser";
    }
    @Override public String getProfileImage() {
        String url = (String) attributes.get("picture");
        return (url != null && !url.isBlank()) ? url : null;
    }
    @Override public SocialProvider getProvider() { return SocialProvider.GOOGLE; }
}

