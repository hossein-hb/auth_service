package com.traazu.auth_service.redis.sign_up;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.traazu.auth_service.redis.AbstractOtpService;
import com.traazu.auth_service.redis.OtpGenerator;
import com.traazu.auth_service.redis.RedisAttemptManager;
import com.traazu.auth_service.redis.RedisLockManager;
import com.traazu.auth_service.redis.prefixes.impl.sign_up.SignUpRedisPrefixes;
import com.traazu.auth_service.services.auth.exceptions.InvalidOtpException;
import com.traazu.auth_service.services.auth.exceptions.InvalidTokenException;
import com.traazu.auth_service.services.auth.exceptions.TokenMismatchException;
import com.traazu.auth_service.services.auth.exceptions.TooManyRequestsException;

public class SignUpOtpService extends AbstractOtpService {

    private final SignUpRedisPrefixes prefixes;

    public SignUpOtpService(StringRedisTemplate redis, OtpGenerator otpGenerator, RedisLockManager lockManager,
            RedisAttemptManager attemptManager, SignUpRedisPrefixes prefixes) {
        super(redis, otpGenerator, lockManager, attemptManager);
        this.prefixes = prefixes;
    }

    public String generateAndSaveOtp(String username) {

        String lockKey = prefixes.getLockPrefix() + username;
        String otpKey = prefixes.getOtpPrefix() + username;
        String otpCode = getOtpGenerator().generateCode();

        Long result = getRedis().execute(
                                            SignUpScripts.OTP_SAVER, 
                                            List.of(lockKey, otpKey),
                                            String.valueOf(prefixes.getLockTTL().toMillis()), 
                                            otpCode, 
                                            String.valueOf(prefixes.getOtpTTL().toMillis())
                                        );

        if (result == null || result == 0L) {
            long remainingSeconds = getLockManager().getRemainingSeconds(username, prefixes);
            throw new TooManyRequestsException(String.format(
                "An OTP has already been sent to your email. " +
                "Please wait %d seconds before requesting a new one.",
                remainingSeconds == 0 ? 120 : remainingSeconds
            ));
        }

        return otpCode;
    }

    public String verifyAndConsumeOtp(String username, String code) {

        if (getLockManager().isEnterOtpLocked(username, prefixes)) {
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please try again later."
            );
        }

        if (getAttemptManager().isAttemptsExceeded(username, prefixes)) {
            getLockManager().tryLockEnterOtp(username, prefixes, prefixes.getLockEnterOtpTTL());
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please wait and request a new OTP."
            );
        }

        String otpKey = prefixes.getOtpPrefix() + username;

        Long result = getRedis().execute(
                                            SignUpScripts.OTP_CONSUMER, 
                                            Collections.singletonList(otpKey), 
                                            code
                                        );

        if (result == null || result == -1L) {
            getAttemptManager().incrementAttempts(username, prefixes);
            throw new InvalidOtpException(
                "OTP has expired or does not exist. Please request a new OTP."
            );
        }

        if (result == 0L) {
            getAttemptManager().incrementAttempts(username, prefixes);
            throw new InvalidOtpException("Invalid OTP code. Please try again.");
        }

        getAttemptManager().resetAttempts(username, prefixes);

        return generateAndSaveToken(username);
    }

    private String generateAndSaveToken(String username) {
        String token = UUID.randomUUID().toString();
        String key = prefixes.getTokenPrefix() + username;

        getRedis().opsForValue().set(key, token, prefixes.getTokenTTL());
        return token;
    }

    public boolean checkToken(String username, String token) {

        if (username == null || token == null) {
            return false;
        }

        String key = prefixes.getTokenPrefix() + username;
        String storedToken = getRedis().opsForValue().get(key);

        return token.equals(storedToken);
    }

    public boolean verifyAndConsumeToken(String username, String token) {
        String key = prefixes.getTokenPrefix() + username;
        
        Long result = getRedis().execute(
                                            SignUpScripts.TOKEN_CONSUMER, 
                                            Collections.singletonList(key),
                                            token
                                        );

        if (result == null || result == -1L) {
            throw new InvalidTokenException("Token has expired or does not exist.");
        } else if (result == 0L) {
            throw new TokenMismatchException("Invalid token!");
        }

        return true;
    }
    
}