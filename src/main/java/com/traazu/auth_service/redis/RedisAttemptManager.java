package com.traazu.auth_service.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.traazu.auth_service.redis.prefixes.RedisPrefixes;

import lombok.AllArgsConstructor;

@Component 
@AllArgsConstructor
public class RedisAttemptManager {

    private final StringRedisTemplate redis;

    private int getAttempts(String username, RedisPrefixes prefixes) {
        String key = getKey(username, prefixes);
        String value = redis.opsForValue().get(key);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void incrementAttempts(String username, RedisPrefixes prefixes) {
        String key = getKey(username, prefixes);
        Long currentAttempts = redis.opsForValue().increment(key);
        if (currentAttempts != null && currentAttempts == 1L) {
            redis.expire(key, prefixes.getAttemptTTL());
        }
    }

    public void resetAttempts(String username, RedisPrefixes prefixes) {
        String key = getKey(username, prefixes);
        redis.delete(key);
    }

    public boolean isAttemptsExceeded(String username, RedisPrefixes prefixes) {
        return getAttempts(username, prefixes) >= prefixes.getMaxAttempts();
    }

    private String getKey(String username, RedisPrefixes prefixes) {
        return prefixes.getAttemptEnterOtpPrefix() + username;
    }
}