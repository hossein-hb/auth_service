package com.traazu.auth_service.domain.dtos.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignUpInfos(

    @NotBlank(message = "The ipAddress has not been defined.")
    String ipAddress,

    @NotBlank(message = "The token has not been defined.")
    String token,

    @Size(max = 20)
    @NotBlank(message = "The firstName has not been defined.")
    String firstName,

    @Size(max = 20)
    @NotBlank(message = "The lastName has not been defined.")
    String lastName,

    @Size(max = 100)
    @NotBlank(message = "The email has not been defined.")
    @Email(message = "The email format is invalid.")
    String email,

    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,100}$",
        message = "The password does not meet the complexity requirements."
    )
    @NotBlank(message = "The password has not been defined.")
    String password,

    @Size(max = 100)
    @NotBlank(message = "The repeatPassword has not been defined.")
    String repeatPassword,

    @NotNull(message = "The deviceInfo has not been defined.")
    @Valid 
    DeviceInfo deviceInfo

) {}
