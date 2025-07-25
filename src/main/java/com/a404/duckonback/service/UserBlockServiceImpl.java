package com.a404.duckonback.service;

import com.a404.duckonback.entity.UserBlock;
import com.a404.duckonback.entity.UserBlockId;
import com.a404.duckonback.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserBlockServiceImpl implements UserBlockService {

    private final UserBlockRepository userBlockRepository;

    @Override
    public UserBlock createUserBlock(UserBlock userBlock) {
        return userBlockRepository.save(userBlock);
    }

    @Override
    public Optional<UserBlock> getUserBlock(Long blockerId, Long blockedId) {
        return userBlockRepository.findById(new UserBlockId(blockerId, blockedId));
    }

    @Override
    public List<UserBlock> getBlocksByBlocker(Long blockerId) {
        return userBlockRepository.findByBlocker_Id(blockerId);
    }

    @Override
    public List<UserBlock> getBlocksByBlocked(Long blockedId) {
        return userBlockRepository.findByBlocked_Id(blockedId);
    }

    @Override
    public boolean isUserBlocked(Long blockerId, Long blockedId) {
        return userBlockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blockedId);
    }

    @Override
    public void deleteUserBlock(Long blockerId, Long blockedId) {
        userBlockRepository.deleteByBlocker_IdAndBlocked_Id(blockerId, blockedId);
    }
}
