package com.traazu.auth_service.services.otp;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.services.otp.prefixes.OtpPrefixes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockOtpService {

    private final StringRedisTemplate redis;

    public boolean isLocked(String username, OtpPrefixes prefixes) {
        return Boolean.TRUE.equals(redis.hasKey(getLockKey(username, prefixes)));
    }

    public boolean tryLock(String username, OtpPrefixes prefixes) {
        return tryLock(username, prefixes, prefixes.getLockTTL());
    }

    public boolean tryLock(String username, OtpPrefixes prefixes, Duration ttl) {
        return !Boolean.TRUE.equals(
            redis.opsForValue().setIfAbsent(getLockKey(username, prefixes), "lock", ttl)
        );
    }

    public long getRemainingSeconds(String username, OtpPrefixes prefixes) {
        Long expire = redis.getExpire(getLockKey(username, prefixes), TimeUnit.SECONDS);
        return (expire != null && expire > 0) ? expire : 0L;
    }

    public void unlock(String username, OtpPrefixes prefixes) {
        redis.delete(getLockKey(username, prefixes));
    }

    public boolean isEnterOtpLocked(String username, OtpPrefixes prefixes) {
        return Boolean.TRUE.equals(redis.hasKey(getLockEnterOtpKey(username, prefixes)));
    }

    public boolean tryLockEnterOtp(String username, OtpPrefixes prefixes) {
        return tryLockEnterOtp(username, prefixes, prefixes.getLockEnterOtpTTL());
    }

    public boolean tryLockEnterOtp(String username, OtpPrefixes prefixes, Duration ttl) {
        return !Boolean.TRUE.equals(
            redis.opsForValue().setIfAbsent(getLockEnterOtpKey(username, prefixes), "lock", ttl)
        );
    }

    public long getRemainingEnterOtpSeconds(String username, OtpPrefixes prefixes) {
        Long expire = redis.getExpire(getLockEnterOtpKey(username, prefixes), TimeUnit.SECONDS);
        return (expire != null && expire > 0) ? expire : 0L;
    }

    public void unlockEnterOtp(String username, OtpPrefixes prefixes) {
        redis.delete(getLockEnterOtpKey(username, prefixes));
    }

    private String getLockKey(String username, OtpPrefixes prefixes) {
        return prefixes.getLockPrefix() + username;
    }

    private String getLockEnterOtpKey(String username, OtpPrefixes prefixes) {
        return prefixes.getLockEnterOtpPrefix() + username;
    }
}