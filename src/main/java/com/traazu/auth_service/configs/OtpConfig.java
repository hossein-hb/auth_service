package com.traazu.auth_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.traazu.auth_service.services.otp.prefixes.ChangePasswordOtpPrefixes;
import com.traazu.auth_service.services.otp.prefixes.SignUpOtpPrefixes;

@Configuration
public class OtpConfig {

    @Bean
    public SignUpOtpPrefixes signUpOtpPrefixes() {
        return new SignUpOtpPrefixes();
    }

    @Bean
    public ChangePasswordOtpPrefixes changePasswordOtpPrefixes() {
        return new ChangePasswordOtpPrefixes();
    }
}