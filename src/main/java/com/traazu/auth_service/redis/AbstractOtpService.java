package com.traazu.auth_service.redis;

import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.traazu.auth_service.services.mail.EmailService;

@Getter
@AllArgsConstructor 
public abstract class AbstractOtpService {
    
    private final StringRedisTemplate redis;
    private final OtpGenerator otpGenerator;
    private final RedisLockManager lockManager;
    private final RedisAttemptManager attemptManager;
    private final EmailService emailService;
    
}