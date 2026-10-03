package com.traazu.auth_service.domain.dtos;

import jakarta.validation.constraints.NotBlank;

public record TokenResponse(
    @NotBlank
    String token,

    @NotBlank
    String message
) {

    public static TokenResponse of(String token) {
        return new TokenResponse(token, "code confirmed.");
    }
}
