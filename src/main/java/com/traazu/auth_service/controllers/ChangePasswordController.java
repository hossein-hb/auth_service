package com.traazu.auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.traazu.auth_service.domain.dtos.ChangePasswordRequest;
import com.traazu.auth_service.domain.dtos.ForgetPasswordRequest;
import com.traazu.auth_service.domain.dtos.MessageResponse;
import com.traazu.auth_service.domain.dtos.TokenResponse;
import com.traazu.auth_service.domain.dtos.auth.OtpVerificationRequest;
import com.traazu.auth_service.services.auth.ForgetPasswordService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController 
@Slf4j
public class ChangePasswordController {

    private final ForgetPasswordService forgetPasswordService;

    public ChangePasswordController(ForgetPasswordService forgetPasswordService) {
        this.forgetPasswordService = forgetPasswordService;
    }

    @PostMapping("/forget-password/send-code")
    public ResponseEntity<MessageResponse> sendVerificationCode(
            @Valid @RequestBody ForgetPasswordRequest request, 
            HttpServletRequest httpServletRequest) {

        String ipAddress = httpServletRequest.getRemoteAddr();
        
        log.info("Request to send a verification code to the email address: {}", request.email());
        MessageResponse message = forgetPasswordService.sendVerificationOtpCode(request, ipAddress);
        return ResponseEntity.ok(message);
    }

    @PostMapping(("/forget-password/verify_code"))
    public ResponseEntity<TokenResponse> verifyCode(@Valid @RequestBody OtpVerificationRequest request) {
        TokenResponse tokenResponse = forgetPasswordService.verifyOtpAndIssueChangePasswordToken(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/forget-password/set-password")
    public ResponseEntity<MessageResponse> setPassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse response = forgetPasswordService.setNewPassword(request);
        return ResponseEntity.ok(response);
    }
    
}
