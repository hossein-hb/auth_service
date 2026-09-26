package com.traazu.auth_service.services.auth.sign_up;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.traazu.auth_service.domain.dtos.CheckIpRequest;
import com.traazu.auth_service.domain.dtos.auth.OtpVerificationRequest;
import com.traazu.auth_service.domain.dtos.auth.SignUpRequest;
import com.traazu.auth_service.domain.dtos.auth.sign_up.SignUpInfos;
import com.traazu.auth_service.domain.entities.BaseUser;
import com.traazu.auth_service.domain.factory.AbstractBaseUserFactory;
import com.traazu.auth_service.redis.IpRateLimiter;
import com.traazu.auth_service.redis.sign_up.SignUpOtpService;
import com.traazu.auth_service.repositories.BaseUserRepository;
import com.traazu.auth_service.services.auth.exceptions.DuplicateEmailException;
import com.traazu.auth_service.services.auth.exceptions.InvalidRegistrationTokenException;
import com.traazu.auth_service.services.auth.exceptions.PasswordMismatchException;

import lombok.Getter;

@Getter
public abstract class SignUpService<T extends BaseUser, I extends SignUpInfos> {

    private final AbstractBaseUserFactory<T, I> baseUserFactory;
    private final BaseUserRepository<T, UUID> baseUserRepository;
    private final SignUpOtpService signUpOtpService;
    private final StringRedisTemplate redis;

    private final Duration ipCoolDownTTL;
    private final Long maxAttempts;
    private final String ipRateLimitPrefix;

    public SignUpService(AbstractBaseUserFactory<T, I> baseUserFactory, BaseUserRepository<T, UUID> baseUserRepository,
            SignUpOtpService signUpOtpService, StringRedisTemplate redis, Duration ipCoolDownTTL, Long maxAttempts,
            String ipRateLimitPrefix) {
        this.baseUserFactory = baseUserFactory;
        this.baseUserRepository = baseUserRepository;
        this.signUpOtpService = signUpOtpService;
        this.redis = redis;
        this.ipCoolDownTTL = ipCoolDownTTL;
        this.maxAttempts = maxAttempts;
        this.ipRateLimitPrefix = ipRateLimitPrefix;
    }

    public String sendVerificationCodeForSignUp(SignUpRequest request) {

        checkIpRateLimit(request.ipAddress());

        if (baseUserRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(
                "Email '" + request.email() + "' is already registered"
            );
        }

        return signUpOtpService.generateAndSaveOtp(request.email());
    }

    public String checkVerificationCodeForSignUp(OtpVerificationRequest request) {
        return signUpOtpService.verifyAndConsumeOtp(request.email(), request.otpCode());
    }

    public T completeInformation(I request) {

        if (!signUpOtpService.checkToken(request.getEmail(), request.getSignUpToken())) {
            throw new InvalidRegistrationTokenException("Invalid or expired signup token");
        }

        if (!request.getPassword().equals(request.getRepeatPassword())) {
            throw new PasswordMismatchException("Password does not match the repeated password!");
        }

        T baseUser = baseUserFactory.create(request);
        baseUserRepository.save(baseUser);

        signUpOtpService.verifyAndConsumeToken(request.getEmail(), request.getSignUpToken());

        return baseUser;

    }

    private void checkIpRateLimit(String ipAddress) {
        String redisIpKey = ipRateLimitPrefix + ipAddress;
        CheckIpRequest checkIpRequest = new CheckIpRequest(redis, redisIpKey, ipCoolDownTTL, maxAttempts);

        IpRateLimiter.checkIp(checkIpRequest);
    }
    
}
