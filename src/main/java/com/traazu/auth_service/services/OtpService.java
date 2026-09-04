package com.traazu.auth_service.services;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final String OTP_PREFIX = "signup:otp:";
    private static final Duration OTP_TTL = Duration.ofMinutes(2);

    @Autowired
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

    private final SecureRandom secureRandom = new SecureRandom();

    private String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    public void genarateAndSaveOtp(String username) {
        String key = OTP_PREFIX + username;
        Object otpCode = generateCode();
        redisTemplate.opsForValue().set(key, otpCode, OTP_TTL);
    }

    public boolean verifyAndConsumeOtp(String username, Object code) {
        Object storedCode = redisTemplate.opsForValue().get(code);
        if (storedCode == null || !storedCode.equals(storedCode)) {
            return false;
        }
        redisTemplate.delete(username);
        return true;
    }
    
}
