package com.traazu.auth_service.security;

import java.util.UUID;

public record TokenInfos(
    UUID id,
    String username,
    String role
) {
    public boolean isValid() {
        return id != null &&
               username != null &&
               role != null &&
               !role.isBlank();
    }
}
