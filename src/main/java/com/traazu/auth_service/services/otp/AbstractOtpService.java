package com.traazu.auth_service.services.otp;

import java.util.Collections;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.traazu.auth_service.services.auth.exceptions.InvalidOtpException;
import com.traazu.auth_service.services.auth.exceptions.TooManyRequestsException;
import com.traazu.auth_service.services.otp.prefixes.OtpPrefixes;

public abstract class AbstractOtpService {

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

    private final LockOtpService lockOtpService;
    private final AttemptOtpService attemptOtpService;
    private final StringRedisTemplate redisTemplate;
    private final OtpGenerator otpGenerator;

    protected AbstractOtpService(LockOtpService lockOtpService,
                                  AttemptOtpService attemptOtpService,
                                  StringRedisTemplate redisTemplate) {
        this.lockOtpService = lockOtpService;
        this.attemptOtpService = attemptOtpService;
        this.redisTemplate = redisTemplate;
        this.otpGenerator = new OtpGenerator();
    }

    protected abstract OtpPrefixes getPrefixes();

    public String generateAndSaveOtp(String username) {
        OtpPrefixes prefixes = getPrefixes();

        if (!lockOtpService.tryLock(username, prefixes)) {
            long remainingSeconds = lockOtpService.getRemainingSeconds(username, prefixes);
            throw new TooManyRequestsException(String.format(
                "An OTP has already been sent to your email. " +
                "Please wait %d seconds before requesting a new one.",
                remainingSeconds == 0 ? 60 : remainingSeconds
            ));
        }

        String key = prefixes.getOtpPrefix() + username;
        String otpCode = otpGenerator.generateCode();
        redisTemplate.opsForValue().set(key, otpCode, prefixes.getOtpTTL());

        return otpCode;
    }

    public String verifyAndConsumeOtp(String username, String code) {
        OtpPrefixes prefixes = getPrefixes();

        if (lockOtpService.isEnterOtpLocked(username, prefixes)) {
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please try again later."
            );
        }

        if (attemptOtpService.isAttemptsExceeded(username, prefixes)) {
            lockOtpService.tryLockEnterOtp(username, prefixes, prefixes.getLockEnterOtpTTL());
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please wait and request a new OTP."
            );
        }

        String otpKey = prefixes.getOtpPrefix() + username;
        RedisScript<Long> script = new DefaultRedisScript<>(OTP_CONSUME_SCRIPT, Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(otpKey), code);

        if (result == null || result == -1L) {
            attemptOtpService.incrementAttempts(username, prefixes);
            throw new InvalidOtpException(
                "OTP has expired or does not exist. Please request a new OTP."
            );
        }

        if (result == 0L) {
            attemptOtpService.incrementAttempts(username, prefixes);
            throw new InvalidOtpException("Invalid OTP code. Please try again.");
        }

        attemptOtpService.resetAttempts(username, prefixes);

        return generateAndSaveToken(username);
    }

    private String generateAndSaveToken(String username) {
        OtpPrefixes prefixes = getPrefixes();
        String token = UUID.randomUUID().toString();
        String key = prefixes.getTokenPrefix() + username;

        redisTemplate.opsForValue().set(key, token, prefixes.getTokenTTL());
        return token;
    }

    public boolean verifyAndConsumeToken(String username, String token) {
        OtpPrefixes prefixes = getPrefixes();
        String key = prefixes.getTokenPrefix() + username;
        String storedToken = redisTemplate.opsForValue().get(key);

        if (storedToken == null || !storedToken.equals(token)) {
            return false;
        }

        redisTemplate.delete(key);
        return true;
    }
    
}