package com.traazu.auth_service.services.auth.exceptions;

public class InvalidRegistrationTokenException extends RuntimeException {
    
    public InvalidRegistrationTokenException(String message) {
        super(message);
    }
    
    public InvalidRegistrationTokenException(String message, Throwable cause) {
        super(message, cause);
    }
    
}