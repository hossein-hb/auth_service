package com.traazu.auth_service.services.otp;

import java.security.SecureRandom;

public class OtpGenerator {

    private final SecureRandom secureRandom;

    public OtpGenerator() {
        this.secureRandom = new SecureRandom();
    }

    public String generateCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
    
}
