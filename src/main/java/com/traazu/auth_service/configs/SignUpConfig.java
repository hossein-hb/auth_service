package com.traazu.auth_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.traazu.auth_service.redis.OtpGenerator;
import com.traazu.auth_service.redis.RedisAttemptManager;
import com.traazu.auth_service.redis.RedisLockManager;
import com.traazu.auth_service.redis.prefixes.impl.sign_up.StaffSignUpRedisPrefixes;
import com.traazu.auth_service.redis.prefixes.impl.sign_up.UserSignUpRedisPrefixes;
import com.traazu.auth_service.redis.sign_up.SignUpOtpService;

@Configuration 
public class SignUpConfig {

    @Bean 
    public SignUpOtpService userSignUpOtpService(StringRedisTemplate redis, 
            UserSignUpRedisPrefixes prefixes, RedisAttemptManager attemptManager, RedisLockManager lockManager, 
            OtpGenerator otpGenerator) {
        return new SignUpOtpService(redis, otpGenerator, lockManager, attemptManager, prefixes);
    }

    @Bean 
    public SignUpOtpService staffSignUpOtpService(StringRedisTemplate redis, 
            StaffSignUpRedisPrefixes prefixes, RedisAttemptManager attemptManager, RedisLockManager lockManager, 
            OtpGenerator otpGenerator) {
        return new SignUpOtpService(redis, otpGenerator, lockManager, attemptManager, prefixes);
    }
    
}
