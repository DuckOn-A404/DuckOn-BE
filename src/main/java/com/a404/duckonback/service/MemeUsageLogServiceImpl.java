package com.a404.duckonback.service;

import com.a404.duckonback.entity.Meme;
import com.a404.duckonback.entity.MemeUsageLog;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.MemeUsageType;
import com.a404.duckonback.exception.CustomException;
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
    public void logMemeUsage(Long userId, Long memeId, MemeUsageType usageType) {
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
        } else if (usageType == MemeUsageType.DOWNLOAD) {
            meme.setDownloadCnt(meme.getDownloadCnt() + 1);
        }

        log.info("Meme usage logged: memeId={}, userId={}, type={}",
                memeId, userId, usageType);

    }
}
