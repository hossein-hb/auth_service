package com.traazu.auth_service.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.traazu.auth_service.domain.entities.User;
import com.traazu.auth_service.domain.enums.AccountStatus;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByAccountStatus(AccountStatus accountStatus, Pageable pageable);
    
}
