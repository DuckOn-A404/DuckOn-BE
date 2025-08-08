package com.a404.duckonback.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private static final String KEY_PREFIX = "blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;

    public TokenBlacklistServiceImpl(RedisTemplate<String,Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklist(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(KEY_PREFIX + token, true, expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(KEY_PREFIX  + token);
    }
}
