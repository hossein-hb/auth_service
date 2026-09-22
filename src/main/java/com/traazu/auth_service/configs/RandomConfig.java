package com.traazu.auth_service.configs;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RandomConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
    
}