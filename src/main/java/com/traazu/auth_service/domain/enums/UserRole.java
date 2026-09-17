package com.traazu.auth_service.domain.enums;

import java.util.Arrays;

public enum UserRole {

    SUPPORT,
    ADMIN,
    USER;

    public static boolean isValid(String role) {
        return role != null && Arrays.stream(values())
                .anyMatch(r -> r.name().equals(role));
    }
    
}
