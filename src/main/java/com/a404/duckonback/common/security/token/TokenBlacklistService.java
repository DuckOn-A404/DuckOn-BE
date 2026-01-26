package com.a404.duckonback.common.security.token;

public interface TokenBlacklistService {
    void blacklist(String token, long ttlMillis);
    boolean isBlacklisted(String token);
}
