package com.traazu.auth_service.services.auth.exceptions;

public class OAuth2AccountExistsException extends RuntimeException {
    
    public OAuth2AccountExistsException(String message) {
        super(message);
    }
    
    public OAuth2AccountExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
}