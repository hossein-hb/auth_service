package com.traazu.auth_service.domain.dtos;

import com.traazu.auth_service.domain.enums.UserRole;

public record ForgetPasswordRequest(
    String ipAddress,
    String email,
    UserRole userRole
) {}
