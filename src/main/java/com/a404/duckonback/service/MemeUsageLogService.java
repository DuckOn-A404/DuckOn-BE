package com.a404.duckonback.service;


import com.a404.duckonback.enums.MemeUsageType;
import com.a404.duckonback.filter.CustomUserPrincipal;

public interface MemeUsageLogService {
    void logMemeUsage(CustomUserPrincipal userPrincipal, Long memeId, MemeUsageType usageType);
}
