package com.traazu.auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.services.auth.AccessTokenService;

@RestController 
@RequestMapping("/api/auth")
public class TokenController {

    private final AccessTokenService accessTokenService;

    public TokenController(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> generateNewAccessToken(
            @CookieValue("refreshToken") String refreshToken) {

        AuthResponse authResponse = accessTokenService.refresh(refreshToken);
        return ResponseEntity.ok(authResponse);
    }
    
}
