package com.a404.duckonback.service;

import com.a404.duckonback.entity.Meme;
import com.a404.duckonback.entity.MemeUsageLog;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.MemeUsageType;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.repository.MemeRepository;
import com.a404.duckonback.repository.MemeUsageLogRepository;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemeUsageLogServiceImpl implements MemeUsageLogService{
    private final MemeRepository memeRepository;
    private final UserRepository userRepository;
    private final MemeUsageLogRepository memeUsageLogRepository;

    @Override
    public void logMemeUsage(CustomUserPrincipal userPrincipal, Long memeId, MemeUsageType usageType) {
        if(userPrincipal == null) {
            throw new CustomException("로그인이 필요합니다.", ErrorCode.USER_NOT_AUTHENTICATED);
        }
        if(memeId == null) {
            throw new CustomException("밈 ID가 필요합니다.", ErrorCode.BAD_REQUEST);
        }
        if(usageType != MemeUsageType.DOWNLOAD && usageType != MemeUsageType.USE) {
            throw new CustomException("유효하지 않은 사용 유형입니다.", ErrorCode.BAD_REQUEST);
        }

        Long userId = userPrincipal.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Invalid user ID: " + userId, ErrorCode.USER_NOT_FOUND));
        Meme meme = memeRepository.findById(memeId)
                .orElseThrow(() -> new CustomException("Invalid meme ID: " + memeId, ErrorCode.NOT_FOUND));

        MemeUsageLog logEntity = MemeUsageLog.builder()
                .user(user)
                .meme(meme)
                .usageType(usageType)
                .createdAt(LocalDateTime.now())
                .build();

        memeUsageLogRepository.save(logEntity);

        // 집계용 카운터 동기 업데이트
        if (usageType == MemeUsageType.USE) {
            meme.setUsageCnt(meme.getUsageCnt() + 1);
        } else {
            meme.setDownloadCnt(meme.getDownloadCnt() + 1);
        }

        log.info("Meme usage logged: memeId={}, userId={}, type={}",
                memeId, userId, usageType);

    }
}
