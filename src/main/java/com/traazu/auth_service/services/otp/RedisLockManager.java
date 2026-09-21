package com.traazu.auth_service.services.otp;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.traazu.auth_service.services.prefixes.RedisPrefixes;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private final StringRedisTemplate redis;

    public boolean isLocked(String username, RedisPrefixes prefixes) {
        return Boolean.TRUE.equals(redis.hasKey(getLockKey(username, prefixes)));
    }

    public boolean tryLock(String username, RedisPrefixes prefixes) {
        return tryLock(username, prefixes, prefixes.getLockTTL());
    }

    public boolean tryLock(String username, RedisPrefixes prefixes, Duration ttl) {
        return !Boolean.TRUE.equals(
            redis.opsForValue().setIfAbsent(getLockKey(username, prefixes), "lock", ttl)
        );
    }

    public long getRemainingSeconds(String username, RedisPrefixes prefixes) {
        Long expire = redis.getExpire(getLockKey(username, prefixes), TimeUnit.SECONDS);
        return (expire != null && expire > 0) ? expire : 0L;
    }

    public void unlock(String username, RedisPrefixes prefixes) {
        redis.delete(getLockKey(username, prefixes));
    }

    public boolean isEnterOtpLocked(String username, RedisPrefixes prefixes) {
        return Boolean.TRUE.equals(redis.hasKey(getLockEnterOtpKey(username, prefixes)));
    }

    public boolean tryLockEnterOtp(String username, RedisPrefixes prefixes) {
        return tryLockEnterOtp(username, prefixes, prefixes.getLockEnterOtpTTL());
    }

    public boolean tryLockEnterOtp(String username, RedisPrefixes prefixes, Duration ttl) {
        return !Boolean.TRUE.equals(
            redis.opsForValue().setIfAbsent(getLockEnterOtpKey(username, prefixes), "lock", ttl)
        );
    }

    public long getRemainingEnterOtpSeconds(String username, RedisPrefixes prefixes) {
        Long expire = redis.getExpire(getLockEnterOtpKey(username, prefixes), TimeUnit.SECONDS);
        return (expire != null && expire > 0) ? expire : 0L;
    }

    public void unlockEnterOtp(String username, RedisPrefixes prefixes) {
        redis.delete(getLockEnterOtpKey(username, prefixes));
    }

    private String getLockKey(String username, RedisPrefixes prefixes) {
        return prefixes.getLockPrefix() + username;
    }

    private String getLockEnterOtpKey(String username, RedisPrefixes prefixes) {
        return prefixes.getLockEnterOtpPrefix() + username;
    }
}