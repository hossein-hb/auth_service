package com.traazu.auth_service.services.otp;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.services.auth.exceptions.InvalidOtpException;
import com.traazu.auth_service.services.auth.exceptions.TooManyRequestsException;
import com.traazu.auth_service.services.otp.enums.OtpTarget;

@Service
public class OtpService {

    private static final Duration SIGNUP_OTP_TTL = Duration.ofMinutes(2);
    private static final Duration SIGNUP_TOKEN_TTL = Duration.ofMinutes(5);
    private static final Duration PENALTY_LOCK_TTL = Duration.ofMinutes(15);

    private final LockOtpService lockOtpService;
    private final AttemptOtpService attemptOtpService;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom;

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

    public OtpService(LockOtpService lockOtpService, AttemptOtpService attemptOtpService,
            StringRedisTemplate redisTemplate) {
        this.lockOtpService = lockOtpService;
        this.attemptOtpService = attemptOtpService;
        this.redisTemplate = redisTemplate;
        this.secureRandom = new SecureRandom();
    }

    private String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    public String generateAndSaveOtp(String username) {
        if (!lockOtpService.tryLock(username, OtpTarget.SIGNUP_LOCK)) {
            Long remainingSeconds = lockOtpService.getRemainingSeconds(username, OtpTarget.SIGNUP_LOCK);
            throw new TooManyRequestsException(String.format(
                "An OTP has already been sent to your email. " +
                "Please wait %d seconds before requesting a new one."
                , remainingSeconds == null ? 60 : remainingSeconds
            ));
        }
        String key = OtpTarget.SIGNUP_OTP.getPrefix() + username;
        String otpCode = generateCode();
        redisTemplate.opsForValue().set(key, otpCode, SIGNUP_OTP_TTL);
        
        return otpCode; 
    }

    public String verifyAndConsumeOtp(String username, String code) {
        if (lockOtpService.isLocked(username, OtpTarget.SIGNUP_LOCK_ENTER_OTP)) {
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please try again later."
            );
        }
        
        if (attemptOtpService.isAttemptsExceeded(username, OtpTarget.SIGNUP_ATTEMPT_ENTER_OTP)) {
            lockOtpService.tryLock(username, OtpTarget.SIGNUP_LOCK_ENTER_OTP, PENALTY_LOCK_TTL);
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please wait 15 minutes and request a new OTP."
            );
        }

        String otpKey = OtpTarget.SIGNUP_OTP.getPrefix() + username;
        RedisScript<Long> script = new DefaultRedisScript<>(OTP_CONSUME_SCRIPT, Long.class);

        Long result = redisTemplate.execute(
            script,
            Collections.singletonList(otpKey),
            code
        );
        
        if (result == null || result == -1L) {
            attemptOtpService.incrementAttempts(username, OtpTarget.SIGNUP_ATTEMPT_ENTER_OTP);
            throw new InvalidOtpException(
                "OTP has expired or does not exist. Please request a new OTP."
            );
        }

        if (result == 0L) {
            attemptOtpService.incrementAttempts(username, OtpTarget.SIGNUP_ATTEMPT_ENTER_OTP);
            throw new InvalidOtpException("Invalid OTP code. Please try again.");
        }

        attemptOtpService.resetAttempts(username, OtpTarget.SIGNUP_ATTEMPT_ENTER_OTP);
        
        return generateAndSaveToken(username);
    }

    private String generateAndSaveToken(String username) {
        String token = UUID.randomUUID().toString();
        String key = OtpTarget.SIGNUP_TOKEN.getPrefix() + username;

        redisTemplate.opsForValue().set(key, token, SIGNUP_TOKEN_TTL);
        return token;
    }

    public boolean verifyAndConsumeToken(String username, String token) {
        String key = OtpTarget.SIGNUP_TOKEN.getPrefix() + username;
        String storedToken = redisTemplate.opsForValue().get(key);
        
        if (storedToken == null || !storedToken.equals(token)) {
            return false;
        }
        
        redisTemplate.delete(key);
        return true;
    }
}