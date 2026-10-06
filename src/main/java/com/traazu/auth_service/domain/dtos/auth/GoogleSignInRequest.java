package com.traazu.auth_service.domain.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequest(

    @NotBlank(message = "Google ID token is required")
    String idToken

) {}