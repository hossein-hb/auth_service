package com.traazu.auth_service.domain.dtos.auth.sign_up.impl;

import java.util.UUID;

import com.traazu.auth_service.domain.dtos.auth.sign_up.SignUpInfos;
import com.traazu.auth_service.domain.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter 
public class StaffSignUpInfos extends SignUpInfos {

    @NotNull(message = "The createdBy has not been defined.")
    private final UUID createdBy;

    @NotBlank(message = "The createdByName has not been defined.")
    private final String createdByName;

    @NotNull(message = "The createdByRole has not been defined.")
    private final UserRole createdByRole;

    public StaffSignUpInfos(String signUpToken, String firstName, String lastName, String email,
            String password, String repeatPassword,
            @NotNull(message = "The createdBy has not been defined.") UUID createdBy,
            @NotBlank(message = "The createdByName has not been defined.") String createdByName,
            @NotNull(message = "The createdByRole has not been defined.") UserRole createdByRole) {
        super(signUpToken, firstName, lastName, email, password, repeatPassword);
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdByRole = createdByRole;
    }
    
}
