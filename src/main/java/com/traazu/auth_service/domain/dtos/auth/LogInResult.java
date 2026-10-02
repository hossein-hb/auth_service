package com.traazu.auth_service.domain.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record LogInResult(
    @NotBlank
    String accessToken,
    @NotBlank 
    String refreshToken
) {}
