package com.traazu.auth_service.controllers;

import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.dtos.auth.ClientDeviceInfo;
import com.traazu.auth_service.domain.dtos.auth.GoogleSignInRequest;
import com.traazu.auth_service.domain.dtos.auth.LogInResult;
import com.traazu.auth_service.services.auth.UserAgentParser;
import com.traazu.auth_service.services.auth.oauth.GoogleOAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class GoogleOAuthController {

    private final GoogleOAuthService googleOAuthService;

    public GoogleOAuthController(
            GoogleOAuthService googleOAuthService) {

        this.googleOAuthService = googleOAuthService;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleSignIn(
            @Valid @RequestBody GoogleSignInRequest request,
            HttpServletRequest httpRequest) {

        String clientIp =
                httpRequest.getRemoteAddr();

        String userAgent =
                httpRequest.getHeader("User-Agent");

        ClientDeviceInfo clientInfo =
                UserAgentParser.parse(userAgent);

        LogInResult result =
                googleOAuthService.authenticate(
                        request.idToken(),
                        clientIp,
                        clientInfo
                );

        ResponseCookie refreshTokenCookie =
                ResponseCookie
                        .from("refreshToken", result.refreshToken())
                        .httpOnly(true)
                        .secure(false)
                        .path("/api/auth/refresh")
                        .maxAge(7 * 24 * 60 * 60)
                        .sameSite("Strict")
                        .build();

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.toString()
                )
                .body(
                        new AuthResponse(result.accessToken())
                );
    }

}