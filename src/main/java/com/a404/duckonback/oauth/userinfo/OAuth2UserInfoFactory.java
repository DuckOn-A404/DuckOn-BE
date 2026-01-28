package com.a404.duckonback.oauth.userinfo;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        switch (registrationId) {
            case "google": return new GoogleUserInfo(attributes);
            case "kakao":  return new KakaoUserInfo(attributes);
            case "naver":  return new NaverUserInfo(attributes);
            default: throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        }
    }
}
