package com.traazu.auth_service.domain.dtos;

import com.traazu.auth_service.domain.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

    @NotBlank(message = "Invalid or expired token.")
    String token,

    @Size(max = 100)
    @NotBlank(message = "The email has not been defined.")
    @Email(message = "The email format is invalid.")
    String email,

    @NotNull(message = "The userRole has not been defined.")
    UserRole userRole,

    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,100}$",
        message = "The password does not meet the complexity requirements."
    )
    @NotBlank(message = "The newPassword has not been defined.")
    String newPassword,

    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,100}$",
        message = "The password does not meet the complexity requirements."
    )
    @NotBlank(message = "The repeatNewPassword has not been defined.")
    String repeatNewPassword
    
) {}
