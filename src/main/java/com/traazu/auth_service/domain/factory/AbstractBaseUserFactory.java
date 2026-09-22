package com.traazu.auth_service.domain.factory;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.traazu.auth_service.domain.dtos.auth.sign_up.SignUpInfos;
import com.traazu.auth_service.domain.entities.BaseUser;
import com.traazu.auth_service.domain.enums.AccountStatus;

import lombok.AllArgsConstructor;

@AllArgsConstructor 
public abstract class AbstractBaseUserFactory<T extends BaseUser, I extends SignUpInfos> {

    private final PasswordEncoder passwordEncoder;

    public T create(I request) {
        T entity = instantiate();
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setEmail(request.getEmail());
        entity.setHashedPassword(passwordEncoder.encode(request.getPassword()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setAccountStatus(AccountStatus.ACTIVE);
        customMapping(entity, request);
        return entity;
    }

    protected abstract T instantiate();
    protected abstract void customMapping(T entity, I request);
}
