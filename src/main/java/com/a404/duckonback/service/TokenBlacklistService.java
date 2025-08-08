package com.a404.duckonback.service;

public interface TokenBlacklistService {
    void blacklist(String token, long ttlMillis);
    boolean isBlacklisted(String token);
}
