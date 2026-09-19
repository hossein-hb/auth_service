package com.traazu.auth_service.domain.dtos.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

    @Size(max = 100)
    @NotBlank(message = "The email has not been defined.")
    @Email(message = "The email format is invalid.")
    String emial,

    @NotBlank(message = "The ipAddress has not been defined.")
    String ipAddress,

    @NotNull(message = "The deviceInfo has not been defined.")
    @Valid
    DeviceInfo deviceInfo
) {}
