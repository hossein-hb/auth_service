package com.traazu.auth_service.services.otp.change_password;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.enums.UserRole;
import com.traazu.auth_service.services.auth.exceptions.InvalidOtpException;
import com.traazu.auth_service.services.auth.exceptions.InvalidTokenException;
import com.traazu.auth_service.services.auth.exceptions.RoleNotFoundException;
import com.traazu.auth_service.services.auth.exceptions.TooManyRequestsException;
import com.traazu.auth_service.services.otp.AbstractOtpService;
import com.traazu.auth_service.services.otp.AttemptOtpService;
import com.traazu.auth_service.services.otp.LockOtpService;
import com.traazu.auth_service.services.otp.prefixes.ChangePasswordOtpPrefixes;

@Service
public class ChangePasswordOtpService extends AbstractOtpService {

    private final ChangePasswordOtpPrefixes prefixes;

    public ChangePasswordOtpService(LockOtpService lockOtpService,
                            AttemptOtpService attemptOtpService,
                            StringRedisTemplate redisTemplate,
                            ChangePasswordOtpPrefixes prefixes) {

        super(lockOtpService, attemptOtpService, redisTemplate);
        this.prefixes = prefixes;
    }

    public String generateAndSaveOtp(String username, UserRole role) {

        String lockKey = prefixes.getLockPrefix() + username;
        String otpKey  = prefixes.getOtpPrefix() + username;
        String roleKey = prefixes.getRoleOtpPrefix() + username;

        String otpCode = getOtpGenerator().generateCode();

        Long result = getRedisTemplate().execute(
                ChangePasswordScripts.OTP_SAVER,
                List.of(lockKey, otpKey, roleKey),
                String.valueOf(prefixes.getLockTTL().toMillis()),
                otpCode,
                String.valueOf(prefixes.getOtpTTL().toMillis()),
                role.name()
        );

        if (result == null || result == 0L) {
            long remainingSeconds = getLockOtpService().getRemainingSeconds(username, prefixes);
            throw new TooManyRequestsException(String.format(
                "An OTP has already been sent to your email. " +
                "Please wait %d seconds before requesting a new one.",
                remainingSeconds == 0 ? 120 : remainingSeconds
            ));
        }

        return otpCode;
    }

    public String verifyAndConsumeOtp(String username, String code) {

        if (getLockOtpService().isEnterOtpLocked(username, prefixes)) {
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please try again later."
            );
        }

        if (getAttemptOtpService().isAttemptsExceeded(username, prefixes)) {
            getLockOtpService().tryLockEnterOtp(username, prefixes, prefixes.getLockEnterOtpTTL());
            throw new TooManyRequestsException(
                "Too many failed OTP attempts. Please wait and request a new OTP."
            );
        }

        String otpKey  = prefixes.getOtpPrefix() + username;
        String roleKey = prefixes.getRoleOtpPrefix() + username;

        String result = getRedisTemplate().execute(ChangePasswordScripts.OTP_CONSUMER, List.of(otpKey, roleKey), code);

        if ("-1".equals(result)) {
            getAttemptOtpService().incrementAttempts(username, prefixes);
            throw new InvalidOtpException("OTP has expired or does not exist. Please request a new OTP.");
        } 
        
        if ("0".equals(result)) {
            getAttemptOtpService().incrementAttempts(username, prefixes);
            throw new InvalidOtpException("Invalid OTP code. Please try again.");
        } 
        
        if (result == null || "ROLE_NOT_FOUND".equals(result) || !UserRole.isValid(result)) {
            throw new RoleNotFoundException("Role not found or expired. Please request a new OTP.");
        }

        getAttemptOtpService().resetAttempts(username, prefixes);

        return generateAndSaveToken(username, result);
    }

    private String generateAndSaveToken(String username, String role) {
        String token = UUID.randomUUID().toString();
        String key = prefixes.getTokenPrefix() + username;

        String value = token + ":" + (role == null ? "" : role);

        getRedisTemplate().opsForValue().set(key, value, prefixes.getTokenTTL());
        return token;
    }

    public UserRole verifyAndConsumeTokenAndReturnRole(String username, String token) {
        String key = prefixes.getTokenPrefix() + username;

        String storedRole = getRedisTemplate().execute(
                ChangePasswordScripts.TOKEN_CONSUMER,
                Collections.singletonList(key),
                token
        );

        if (storedRole == null || storedRole.isBlank()) {
            throw new InvalidTokenException("Invalid token!");
        }

        try {
            return UserRole.valueOf(storedRole);
        } catch (IllegalArgumentException e) {
            throw new RoleNotFoundException("Role not found. Invalid token!");
        }
    }

}