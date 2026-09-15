package com.traazu.auth_service.services.otp;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.services.otp.prefixes.OtpPrefixes;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AttemptOtpService {

    private final StringRedisTemplate redis;

    private int getAttempts(String username, OtpPrefixes otpPrefixes) {
        String key = getKey(username, otpPrefixes);
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

    public void incrementAttempts(String username, OtpPrefixes otpPrefixes) {
        String key = getKey(username, otpPrefixes);
        Long currentAttempts = redis.opsForValue().increment(key);
        if (currentAttempts != null && currentAttempts == 1L) {
            redis.expire(key, otpPrefixes.getAttemptTTL());
        }
    }

    public void resetAttempts(String username, OtpPrefixes otpPrefixes) {
        String key = getKey(username, otpPrefixes);
        redis.delete(key);
    }

    public boolean isAttemptsExceeded(String username, OtpPrefixes otpPrefixes) {
        return getAttempts(username, otpPrefixes) >= otpPrefixes.getMaxAttempts();
    }

    private String getKey(String username, OtpPrefixes otpPrefixes) {
        return otpPrefixes.getAttemptEnterOtpPrefix() + username;
    }
}