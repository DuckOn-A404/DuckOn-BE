package com.a404.duckonback.service;


import com.a404.duckonback.enums.MemeUsageType;

public interface MemeUsageLogService {
    void logMemeUsage(Long userId, Long memeId, MemeUsageType usageType);
}
