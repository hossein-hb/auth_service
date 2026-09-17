package com.traazu.auth_service.services.otp;

import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.Getter;

@Getter
public abstract class AbstractOtpService {
            

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

    
}