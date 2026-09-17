package com.traazu.auth_service.domain.dtos;

import com.traazu.auth_service.domain.enums.UserRole;

public record ChangePasswordRequest(
    String ipAddress,
    String token,
    String email,
    UserRole userRole,
    String newPassword,
    String repeatNewPassword
) {}
