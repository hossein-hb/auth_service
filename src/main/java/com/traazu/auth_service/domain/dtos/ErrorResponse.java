package com.traazu.auth_service.domain.dtos;

import java.time.LocalDateTime;

public record ErrorResponse (
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}
