package com.traazu.auth_service.services.otp;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.services.otp.enums.OtpTarget;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class LockOtpService {

    private final StringRedisTemplate redis;
    private static final Duration DEFAULT_COOLDOWN = Duration.ofSeconds(60);

    public boolean isLocked(String username, OtpTarget target) {
        return Boolean.TRUE.equals(redis.hasKey(getKey(username, target)));
    }

    public boolean tryLock(String username, OtpTarget target) {
        return tryLock(username, target, DEFAULT_COOLDOWN);
    }

    public boolean tryLock(String username, OtpTarget target, Duration ttl) {
        return !Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(getKey(username, target), "lock", ttl));
    }

    public long getRemainingSeconds(String username, OtpTarget target) {
        Long expire = redis.getExpire(getKey(username, target), TimeUnit.SECONDS);
        return (expire != null && expire > 0) ? expire : 0L;
    }

    private String getKey(String username, OtpTarget target) {
        return target.getPrefix() + username;
    }
}