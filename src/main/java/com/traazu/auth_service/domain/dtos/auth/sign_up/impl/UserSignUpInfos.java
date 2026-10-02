package com.traazu.auth_service.domain.dtos.auth.sign_up.impl;

import com.traazu.auth_service.domain.dtos.auth.sign_up.SignUpInfos;

import lombok.Getter;

@Getter
public class UserSignUpInfos extends SignUpInfos {

    public UserSignUpInfos(String signUpToken, String firstName, String lastName, String email,
            String password, String repeatPassword) {

        super(signUpToken, firstName, lastName, email, password, repeatPassword);

    }
    
}
