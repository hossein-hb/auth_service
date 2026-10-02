package com.traazu.auth_service.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.dtos.auth.ClientDeviceInfo;
import com.traazu.auth_service.domain.dtos.auth.LogInRequest;
import com.traazu.auth_service.domain.dtos.auth.LogInResult;
import com.traazu.auth_service.services.auth.UserAgentParser;
import com.traazu.auth_service.services.auth.log_in.LogInService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController 
@RequestMapping("/api/auth")
public class LogInController {

    private final LogInService logInService;

    public LogInController(LogInService logInService) {
        this.logInService = logInService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> logIn(
            @Valid @RequestBody LogInRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();

        String userAgentHeader = httpRequest.getHeader("User-Agent");
        ClientDeviceInfo cliendInfo = UserAgentParser.parse(userAgentHeader);

        log.info("Log in request from IP: {} | Device: {} ({}) | User: {}", 
                clientIp, 
                cliendInfo.browser(), 
                cliendInfo.os(), 
                request.username());

        LogInResult result = logInService.logIn(request, clientIp, cliendInfo);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new AuthResponse(result.accessToken()));
    }
}