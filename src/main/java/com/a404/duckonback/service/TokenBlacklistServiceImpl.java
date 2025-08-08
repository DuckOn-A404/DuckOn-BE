package com.a404.duckonback.service;

import com.a404.duckonback.config.BlacklistProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redis;
    private final TokenKeyEncoder encoder;
    private final BlacklistProperties props;

    public TokenBlacklistServiceImpl(StringRedisTemplate redis,
                                     TokenKeyEncoder encoder,
                                     BlacklistProperties props) {
        this.redis = redis;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void blacklist(String token, long expirationMillis) {
        if (token == null || token.isBlank()) return;
        long ttl = Math.max(1L, expirationMillis); // 0/음수 방지
        String key = props.getKeyPrefix() + encoder.encode(token);
        redis.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;
        String key = props.getKeyPrefix() + encoder.encode(token);
        return redis.hasKey(key);
    }
}
