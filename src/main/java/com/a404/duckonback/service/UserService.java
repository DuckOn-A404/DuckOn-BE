package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.SocialProvider;

import java.util.Optional;

public interface UserService {
    User findByEmail(String email);
    User findByUserId(String userId);
    void save(User user);
    boolean isEmailDuplicate(String email);
    boolean isUserIdDuplicate(String userId);
    boolean isNicknameDuplicate(String nickname);

    UserInfoResponseDTO getUserInfo(String myUserId, String otherUserId);
    UserDetailInfoResponseDTO getUserDetailInfo(String userId);
    void deleteUser(User user, String refreshToken);

    FollowersResponseDTO getFollowers(String userId);

    FollowingResponseDTO getFollowing(String userId);

    void followUser(String myUserId, String otherUserId);
    void unfollowUser(String userId, String otherUserId);

    void updateUserInfo(String userId, UpdateProfileRequestDTO newUserInfo);

    boolean verifyPassword(String userId, String inputPassword);

    User findActiveByEmail(String email);
    User findActiveByUserId(String userId);

    default Optional<User> findActiveByProviderAndProviderId(SocialProvider provider, String providerId) { return Optional.empty(); }

    RecommendUsersResponseDTO recommendUsers(String myUserId, Long artistId, int size, boolean includeReasons); // 사용자 추천 기능

    User findByNickname(String nickname);
}
