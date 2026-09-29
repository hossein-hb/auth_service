package com.traazu.auth_service.services.mail.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.services.mail.EmailService;

@Service 
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Traazu - Email Verification");
        message.setText(
            """
            Hello,

            Your verification code is:

            %s

            This code will expire soon.

            If you did not request this code, you can safely ignore this email.

            Regards,
            Traazu
            """.formatted(otp)
        );

        mailSender.send(message);
        
    }
    
}
