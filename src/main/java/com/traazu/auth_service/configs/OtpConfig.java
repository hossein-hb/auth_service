package com.traazu.auth_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.traazu.auth_service.redis.prefixes.impl.ChangePasswordRedisPrefixes;
import com.traazu.auth_service.redis.prefixes.impl.sign_up.StaffSignUpRedisPrefixes;
import com.traazu.auth_service.redis.prefixes.impl.sign_up.UserSignUpRedisPrefixes;

@Configuration
public class OtpConfig {

    @Bean
    public UserSignUpRedisPrefixes userSignUpRedisPrefixes() {
        return new UserSignUpRedisPrefixes();
    }

    @Bean
    public StaffSignUpRedisPrefixes staffSignUpRedisPrefixes() {
        return new StaffSignUpRedisPrefixes();
    }

    @Bean
    public ChangePasswordRedisPrefixes changePasswordRedisPrefixes() {
        return new ChangePasswordRedisPrefixes();
    }
}