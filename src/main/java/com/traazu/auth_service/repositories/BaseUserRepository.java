package com.traazu.auth_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.traazu.auth_service.domain.entities.BaseUser;

@NoRepositoryBean 
public interface BaseUserRepository<T extends BaseUser, UUID> extends JpaRepository<T, UUID> {

    Optional<T> findByEmail(String email);

    boolean existsByEmail(String email);
    
}
