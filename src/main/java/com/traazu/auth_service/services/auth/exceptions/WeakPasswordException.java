package com.traazu.auth_service.services.auth.exceptions;

public class WeakPasswordException extends RuntimeException {
    
    public WeakPasswordException(String message) {
        super(message);
    }
    
    public WeakPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
