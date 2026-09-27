package com.traazu.auth_service.domain.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class MessageResponse {
    
    @NotBlank
    private String message;

    @NotNull
    private boolean success;

    @NotNull
    private LocalDateTime timestamp;

    public MessageResponse(String message) {
        this.message = message;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }

    public MessageResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }
    
}