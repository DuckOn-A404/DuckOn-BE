package com.a404.duckonback.service;

import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.dto.UserInfoResponseDTO;
import com.a404.duckonback.entity.User;

public interface UserService {
    User findByEmail(String email);
    User findByUserId(String userId);
    void save(User user);
    boolean isEmailDuplicate(String email);
    boolean isUserIdDuplicate(String userId);
    boolean isNicknameDuplicate(String nickname);

    UserDetailInfoResponseDTO getUserDetailInfo(String accessToken);
    void deleteUser(String authorization, String refreshToken);

    UserInfoResponseDTO getUserInfo(String userId);
}
