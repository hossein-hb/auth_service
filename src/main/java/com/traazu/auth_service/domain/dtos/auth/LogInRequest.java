package com.traazu.auth_service.domain.dtos.auth;

import com.traazu.auth_service.domain.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogInRequest(

    @NotBlank(message = "The ipAddress has not been defined.")
    String ipAddress,

    @NotBlank(message = "The username has not been defined.")
    String username,

    @NotBlank(message = "The password has not been defined.")
    String password,

    @NotNull(message = "The role has not been defined.")
    UserRole role,

    @NotNull(message = "The deviceInfo has not been defined.")
    DeviceInfo deviceInfo
    
) {}
