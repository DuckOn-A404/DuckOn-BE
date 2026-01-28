package com.a404.duckonback.domain.meme.service;


import com.a404.duckonback.common.enums.MemeUsageType;
import com.a404.duckonback.common.filter.CustomUserPrincipal;

public interface MemeUsageLogService {
    void logMemeUsage(CustomUserPrincipal userPrincipal, Long memeId, MemeUsageType usageType);
}
