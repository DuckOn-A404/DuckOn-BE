package com.a404.duckonback.service;

import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.entity.User;

public interface UserService {
    User findByEmail(String email);
    User findByUserId(String userId);
    void save(User user);
    boolean isEmailDuplicate(String email);
    boolean isUserIdDuplicate(String userId);
    boolean isNicknameDuplicate(String nickname);
    UserDetailInfoResponseDTO getUserInfo(String accessToken);
}
