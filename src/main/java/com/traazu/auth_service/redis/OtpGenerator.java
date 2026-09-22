package com.traazu.auth_service.redis;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component 
public class OtpGenerator {

    private final SecureRandom secureRandom;

    public OtpGenerator(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
    
}
