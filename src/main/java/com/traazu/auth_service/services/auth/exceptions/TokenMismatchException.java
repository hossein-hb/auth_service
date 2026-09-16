package com.traazu.auth_service.services.auth.exceptions;

public class TokenMismatchException extends RuntimeException {
    
    public TokenMismatchException(String message) {
        super(message);
    }
    
    public TokenMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
    
}