package com.traazu.auth_service.domain.dtos.auth;

public record ClientDeviceInfo(
    String browser,
    String os,
    String deviceType,
    String rawUserAgent
) {}
