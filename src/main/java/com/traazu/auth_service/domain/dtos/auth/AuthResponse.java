package com.traazu.auth_service.domain.dtos.auth;


public record AuthResponse(
    String accessToken,
    String refreshToken
) {}
