package com.traazu.auth_service.services.auth.exceptions;

public class PasswordMismatchException extends RuntimeException {
    
    public PasswordMismatchException(String message) {
        super(message);
    }
    
    public PasswordMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
    
}