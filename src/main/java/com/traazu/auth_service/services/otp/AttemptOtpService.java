package com.traazu.auth_service.services.otp;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.services.otp.enums.OtpTarget;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor 
public class AttemptOtpService {

    private final StringRedisTemplate redis;
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPT_TTL = Duration.ofMinutes(10);

    private int getAttempts(String username, OtpTarget otpTarget) {
        String key = getKey(username, otpTarget);
        Object value = redis.opsForValue().get(key);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void incrementAttempts(String username, OtpTarget otpTarget) {
        String key = getKey(username, otpTarget);
        Long currentAttempts = redis.opsForValue().increment(key);
        if (currentAttempts != null && currentAttempts == 1L) {
            redis.expire(key, ATTEMPT_TTL);
        }
    }

    public void resetAttempts(String username, OtpTarget otpTarget) {
        String key = getKey(username, otpTarget);
        redis.delete(key);
    }

    public boolean isAttemptsExceeded(String username, OtpTarget otpTarget) {
        return getAttempts(username, otpTarget) >= MAX_ATTEMPTS;
    }

    private String getKey(String username, OtpTarget target) {
        return target.getPrefix() + username;
    }
    
}
