package com.traazu.auth_service.services.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.services.auth.exceptions.InvalidOtpException;
import com.traazu.auth_service.services.auth.exceptions.TooManyRequestsException;

@Service
public class OtpService {

    private static final String OTP_PREFIX = "signup:otp:";
    private static final Duration OTP_TTL = Duration.ofMinutes(2);
    
    private static final String COOLDOWN_PREFIX = "signup:lock:";
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    
    private static final String TOKEN_PREFIX = "signup:token:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(5);

    private static final String OTP_ATTEMPT_PREFIX = "signup:attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPT_TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String OTP_CONSUME_SCRIPT = 
            "local stored_otp = redis.call('GET', KEYS[1]) " +
            "if stored_otp == false then " +
            "    return -1 " +
            "elseif stored_otp == ARGV[1] then " +
            "    redis.call('DEL', KEYS[1]) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    public OtpService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long getCooldownRemainingSeconds(String username) {
        String key = COOLDOWN_PREFIX + username;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    private String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    public boolean isLocked(String username) {
        String key = COOLDOWN_PREFIX + username;
        return !Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "lock", COOLDOWN_TTL));        
    }

    public String generateAndSaveOtp(String username) {
        if (isLocked(username)) {
            Long remainingSeconds = getCooldownRemainingSeconds(username);
            throw new TooManyRequestsException(String.format(
                "An OTP has already been sent to your email. " +
                "Please wait %d seconds before requesting a new one."
                , remainingSeconds == null ? 60 : remainingSeconds
            ));
        }
        String key = OTP_PREFIX + username;
        String otpCode = generateCode();
        redisTemplate.opsForValue().set(key, otpCode, OTP_TTL);
        
        return otpCode; 
    }

    public String verifyAndConsumeOtp(String username, String code) {
        if (isAttemptsExceeded(OTP_ATTEMPT_PREFIX, username)) {
            String cooldownKey = COOLDOWN_PREFIX + username;
            redisTemplate.opsForValue().set(cooldownKey, "blocked", Duration.ofMinutes(15));
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please wait 15 minutes and request a new OTP."
            );
        }

        String otpKey = OTP_PREFIX + username;

        RedisScript<Long> script = new DefaultRedisScript<>(OTP_CONSUME_SCRIPT, Long.class);

        Long result = redisTemplate.execute(
            script,
            Collections.singletonList(otpKey),
            code
        );
        
        if (result == null || result == -1L) {
            incrementAttempts(OTP_ATTEMPT_PREFIX, username);
            throw new InvalidOtpException(
                "OTP has expired or does not exist. Please request a new OTP."
            );
        }

        if (result == 0L) {
            incrementAttempts(OTP_ATTEMPT_PREFIX, username);
            throw new InvalidOtpException(
                "Invalid OTP code. Please try again. " +
                "(" + (MAX_ATTEMPTS - getAttempts(OTP_ATTEMPT_PREFIX, username)) + " attempts remaining)"
            );
        }

        resetAttempts(OTP_ATTEMPT_PREFIX, username);
        
        return generateAndSaveToken(username);
    }

    private String generateAndSaveToken(String username) {
        String token = UUID.randomUUID().toString();
        String key = TOKEN_PREFIX + username;

        redisTemplate.opsForValue().set(key, token, TOKEN_TTL);
        return token;
    }

    public boolean verifyAndConsumeToken(String username, String token) {
        String key = TOKEN_PREFIX + token;
        Object storedUsername = redisTemplate.opsForValue().get(key);
        
        if (storedUsername == null || !storedUsername.toString().equals(username)) {
            return false;
        }
        
        redisTemplate.delete(key);
        return true;
    }

    private int getAttempts(String prefix, String username) {
        String key = prefix + username;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void incrementAttempts(String prefix, String username) {
        String key = prefix + username;
        Long currentAttempts = redisTemplate.opsForValue().increment(key);
        if (currentAttempts != null && currentAttempts == 1L) {
            redisTemplate.expire(key, ATTEMPT_TTL);
        }
    }

    private void resetAttempts(String prefix, String username) {
        String key = prefix + username;
        redisTemplate.delete(key);
    }

    private boolean isAttemptsExceeded(String prefix, String username) {
        return getAttempts(prefix, username) >= MAX_ATTEMPTS;
    }

}