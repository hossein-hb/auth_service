package com.traazu.auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traazu.auth_service.domain.dtos.BaseUserProfileDto;
import com.traazu.auth_service.domain.dtos.MessageResponse;
import com.traazu.auth_service.domain.dtos.TokenResponse;
import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.dtos.auth.ClientDeviceInfo;
import com.traazu.auth_service.domain.dtos.auth.OtpVerificationRequest;
import com.traazu.auth_service.domain.dtos.auth.SignUpRequest;
import com.traazu.auth_service.domain.dtos.auth.sign_up.impl.StaffSignUpInfos;
import com.traazu.auth_service.domain.dtos.auth.sign_up.impl.UserSignUpInfos;
import com.traazu.auth_service.services.auth.UserAgentParser;
import com.traazu.auth_service.services.auth.sign_up.impl.StaffSignUpService;
import com.traazu.auth_service.services.auth.sign_up.impl.UserSignUpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestController 
@RequestMapping("/api/auth")
public class SignUpController {

    private final UserSignUpService userSignUpService;
    private final StaffSignUpService staffSignUpService;

    public SignUpController(UserSignUpService userSignUpService, StaffSignUpService staffSignUpService) {
        this.userSignUpService = userSignUpService;
        this.staffSignUpService = staffSignUpService;
    }

    @PostMapping("/signup/send-code")
    public ResponseEntity<MessageResponse> sendVerificationCode(
            @Valid @RequestBody SignUpRequest request, 
            HttpServletRequest httpServletRequest) {

        String ipAddress = httpServletRequest.getRemoteAddr();
        
        log.info("Request to send a verification code to the email address: {}", request.email());
        MessageResponse message = userSignUpService.sendVerificationCodeForSignUp(request, ipAddress);
        return ResponseEntity.ok(message);
    }

    @PostMapping(("/signup/verify_code"))
    public ResponseEntity<TokenResponse> verifyCode(@Valid @RequestBody OtpVerificationRequest request) {
        TokenResponse tokenResponse = userSignUpService.checkVerificationCodeForSignUp(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/staff/complete_signup")
    public ResponseEntity<BaseUserProfileDto> completeStaffSignUp(@Valid @RequestBody StaffSignUpInfos staffSignUpInfos) {
        BaseUserProfileDto baseUserProfileDto = staffSignUpService.completeSignUp(staffSignUpInfos);
        return ResponseEntity.ok(baseUserProfileDto);
    }

    @PostMapping("/user/complete_signup")
    public ResponseEntity<AuthResponse> completeUserSignUp(
                @Valid @RequestBody UserSignUpInfos userSignUpInfos, 
                HttpServletRequest httpServletRequest) {

        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        ClientDeviceInfo clientInfo = UserAgentParser.parse(userAgent);

        AuthResponse authResponse = userSignUpService.completeSignUp(userSignUpInfos, ipAddress, clientInfo);
        return ResponseEntity.ok(authResponse);
    }
    
}
