package com.traazu.auth_service.services.mail;

public interface EmailService {

    void sendOtp(String email, String otp);
    
}
