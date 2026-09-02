package com.traazu.auth_service.domain.dtos;

import java.time.LocalDateTime;

import com.traazu.auth_service.domain.enums.AccountStatus;
import com.traazu.auth_service.domain.enums.UserRole;

public record BaseUserProfileDto(
    String firstName,
    String lastName,
    String email,
    UserRole role,
    LocalDateTime createdAt,
    AccountStatus accountStatus
) {}
