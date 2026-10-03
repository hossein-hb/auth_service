package com.traazu.auth_service.domain.dtos;

import com.traazu.auth_service.domain.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ForgetPasswordRequest(
    @Size(max = 100)
    @NotBlank(message = "The email has not been defined.")
    @Email(message = "The email format is invalid.")
    String email,

    @NotNull 
    UserRole userRole
) {}
