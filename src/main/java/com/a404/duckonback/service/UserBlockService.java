package com.a404.duckonback.service;

import com.a404.duckonback.entity.UserBlock;

import java.util.List;
import java.util.Optional;

public interface UserBlockService {
    UserBlock createUserBlock(UserBlock userBlock);
    Optional<UserBlock> getUserBlock(Long blockerId, Long blockedId);
    List<UserBlock> getBlocksByBlocker(Long blockerId);
    List<UserBlock> getBlocksByBlocked(Long blockedId);
    boolean isUserBlocked(Long blockerId, Long blockedId);
    void deleteUserBlock(Long blockerId, Long blockedId);

    /**
     * 다른 사용자를 차단
     * @param blockerId   차단 요청자 PK (Long)
     * @param blockedUserId  차단 대상자의 userId (String)
     */
    void blockUser(Long blockerId, String blockedUserId);
}
