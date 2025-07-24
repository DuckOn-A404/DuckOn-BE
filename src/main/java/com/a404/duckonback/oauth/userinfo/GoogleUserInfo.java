package com.a404.duckonback.oauth.userinfo;

import com.a404.duckonback.enums.SocialProvider;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    public GoogleUserInfo(Map<String, Object> attributes){ this.attributes = attributes; }

    @Override public String getProviderId() { return (String) attributes.get("sub"); }
    @Override public String getEmail() { return (String) attributes.get("email"); }
    @Override public String getNickname() { return (String) attributes.getOrDefault("name", "googleUser"); }
    @Override public String getProfileImage() { return (String) attributes.get("picture"); }
    @Override public SocialProvider getProvider() { return SocialProvider.GOOGLE; }
}
