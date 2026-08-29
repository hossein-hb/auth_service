package com.traazu.auth_service.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;


import com.traazu.auth_service.domain.enums.AccountStatus;
import com.traazu.auth_service.domain.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "First name is required!")
    @Column(name = "first_name", nullable = false)
    @Size(max = 20, message = "First name must be less than 20 characters!")
    private String firstName;

    @NotBlank(message = "Last name is required!")
    @Column(name = "last_name", nullable = false)
    @Size(max = 20, message = "last name must be less than 20 characters!")
    private String lastName;

    @Email(message = "Email format is invalid!")
    @NotBlank(message = "Email is required!")
    @Column(name = "email", nullable = false, updatable = false, unique = true)
    private String email;

    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*?])[A-Za-z\\d!@#$%^&*?]{8,20}$",
        message = "Password must contain lowercase letters, uppercase letters, numbers, and at lease one special character like !, @, #, $, %, ^, &, * or ?"
    )
    @Column(name = "password", nullable = false, updatable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Choosing role is required!")
    @Column(name = "role", nullable = false)
    private UserRole role;

    @NotNull(message = "account status is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
}
