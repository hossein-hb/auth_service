package com.traazu.auth_service.domain.dtos.auth;

import java.util.UUID;

import com.traazu.auth_service.domain.enums.UserRole;

public record StaffSignUpRequest(
    String firstName,
    String lastName,
    String email,
    String password,
    String repeatPassword,
    UserRole role,
    UUID createdBy,
    String createdByName,
    UserRole createdByRole
) {}
