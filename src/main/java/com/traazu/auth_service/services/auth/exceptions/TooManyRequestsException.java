package com.traazu.auth_service.services.auth.exceptions;

public class TooManyRequestsException extends RuntimeException {
    
    public TooManyRequestsException(String message) {
        super(message);
    }
    
    public TooManyRequestsException(String message, Throwable cause) {
        super(message, cause);
    }
    
}