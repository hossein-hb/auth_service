package com.traazu.auth_service.domain.entities;

import java.util.UUID;

import com.traazu.auth_service.domain.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "staffs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Staff extends BaseUser {

    @NotNull(message = "\'created_by\' field is required!")
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @NotBlank(message = "\'craeted_by_name\' field is required!")
    @Column(name = "created_by_name", updatable = false)
    private String createdByName;

    @NotNull(message = "\'created_by_role\' field is required!")
    @Enumerated(EnumType.STRING)
    @Column(name = "created_by_role", updatable = false)
    private UserRole createdByRole;
    
}
