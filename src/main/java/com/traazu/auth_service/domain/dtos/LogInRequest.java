package com.traazu.auth_service.domain.dtos;

import com.traazu.auth_service.domain.enums.UserRole;

public record LogInRequest(
    String username,
    String password,
    UserRole role
) {}
