package com.traazu.auth_service.services.auth.exceptions;

public class InvalidChangePasswordTokenException extends RuntimeException {
    
    public InvalidChangePasswordTokenException(String message) {
        super(message);
    }
    
    public InvalidChangePasswordTokenException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
