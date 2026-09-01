package com.traazu.auth_service.domain.entities;


import com.traazu.auth_service.domain.enums.UserRole;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseUser {

    public User() {
        setRole(UserRole.USER);
    }
    
}
