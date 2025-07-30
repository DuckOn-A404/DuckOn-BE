package com.a404.duckonback.service;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.entity.UserBlock;
import com.a404.duckonback.entity.UserBlockId;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.UserBlockRepository;
import com.a404.duckonback.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserBlockServiceImpl implements UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

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

    @Override
    @Transactional
    public void blockUser(Long blockerId, String blockedUserId) {
        // 1) 차단 요청자 검증
        User blocker = userRepository.findById(blockerId);
        if (blocker == null) {
            throw new CustomException("차단 요청 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        // 2) 차단 대상자 검증 (userId 기준)
        User blocked = userRepository.findByUserId(blockedUserId);
        if (blocked == null) {
            throw new CustomException("차단 대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        // 3) 자기 자신 차단 금지
        if (blocker.getId().equals(blocked.getId())) {
            throw new CustomException("자기 자신을 차단할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        // 4) 중복 차단 검사
        if (userBlockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blocked.getId())) {
            throw new CustomException("이미 차단된 사용자입니다.", HttpStatus.BAD_REQUEST);
        }
        // 5) 저장
        UserBlock block = UserBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .createdAt(LocalDateTime.now())
                .build();
        userBlockRepository.save(block);
    }

    @Override
    @Transactional
    public void deleteUserBlock(Long blockerId, String blockedUserId) {
        // 1) 차단 요청자 검증
        User blocker = userRepository.findById(blockerId);
        if (blocker == null) {
            throw new CustomException("차단 요청 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        // 2) 차단 해제 대상자 검증
        User blocked = userRepository.findByUserId(blockedUserId);
        if (blocked == null) {
            throw new CustomException("차단 해제 대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        // 3) 삭제
        userBlockRepository.deleteByBlocker_IdAndBlocked_Id(blockerId, blocked.getId());
    }

    @Override
    public boolean isUserBlocked(Long blockerId, String blockedUserId) {
        // 1) 차단 요청자 검증
        User blocker = userRepository.findById(blockerId);
        if (blocker == null) {
            throw new CustomException("차단 요청 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        // 2) 차단 대상자 검증
        User blocked = userRepository.findByUserId(blockedUserId);
        if (blocked == null) {
            throw new CustomException("차단 대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        // 3) 차단 여부 확인
        return userBlockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blocked.getId());
    }

}
