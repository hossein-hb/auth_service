package com.traazu.auth_service.domain.dtos;

public record UserSignUpRequest(
    String firstName,
    String lastName,
    String email,
    String password,
    String repeatPassword
) {}
