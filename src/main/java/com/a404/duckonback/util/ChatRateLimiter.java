package com.a404.duckonback.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ChatRateLimiter {

    private final StringRedisTemplate redisTemplate;


    public boolean allow(String key, int limit, Duration window) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            // 첫 증가 시에만 TTL 설정
            redisTemplate.expire(key, window);
        }
        return count != null && count <= limit;
    }

    public String userKey(String userId) {
        return "chat_limit:user:" + userId;
    }
}
