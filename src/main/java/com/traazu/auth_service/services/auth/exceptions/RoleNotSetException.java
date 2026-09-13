package com.traazu.auth_service.services.auth.exceptions;

public class RoleNotSetException extends RuntimeException {
    
    public RoleNotSetException(String message) {
        super(message);
    }
    
    public RoleNotSetException(String message, Throwable cause) {
        super(message, cause);
    }
    
}