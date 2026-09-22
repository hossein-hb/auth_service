package com.traazu.auth_service.domain.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerificationRequest(

    @Size(max = 100)
    @NotBlank(message = "The email has not been defined.")
    @Email(message = "The email format is invalid.")
    String email,

    String otpCode
    
) {}
