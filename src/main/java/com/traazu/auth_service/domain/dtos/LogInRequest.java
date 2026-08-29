package com.traazu.auth_service.domain.dtos;

public record LogInRequest(
    String username,
    String password
) {}
