package com.a404.duckonback.common.oauth.userinfo;

import com.a404.duckonback.common.enums.SocialProvider;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getNickname();
    String getProfileImage();
    SocialProvider getProvider();
}
