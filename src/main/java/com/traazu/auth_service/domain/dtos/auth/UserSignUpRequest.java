package com.traazu.auth_service.domain.dtos.auth;

public record UserSignUpRequest(
    String token,
    String firstName,
    String lastName,
    String email,
    String password,
    String repeatPassword,
    DeviceInfo deviceInfo
) {}
