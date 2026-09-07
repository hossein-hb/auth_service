package com.traazu.auth_service.domain.dtos.auth;

public record OtpVerificationRequest(
    String email,
    String otpCode
) {}
