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
}
