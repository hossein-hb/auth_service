package com.traazu.auth_service.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.traazu.auth_service.domain.entities.Staff;
import com.traazu.auth_service.domain.enums.AccountStatus;

public interface StaffRepository extends JpaRepository<Staff, UUID> {

    Optional<Staff> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Staff> findByAccountStatus(AccountStatus accountStatus, Pageable pageable);
    
}
