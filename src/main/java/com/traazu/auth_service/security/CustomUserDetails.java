package com.traazu.auth_service.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.traazu.auth_service.domain.entities.BaseUser;
import com.traazu.auth_service.domain.enums.AccountStatus;
import com.traazu.auth_service.domain.enums.UserRole;

import jakarta.annotation.Nullable;

public class CustomUserDetails implements UserDetails {

    private BaseUser baseUser;

    public CustomUserDetails(BaseUser baseUser) {
        this.baseUser = baseUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + baseUser.getRole().name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return baseUser.getAccountStatus() != AccountStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return baseUser.getAccountStatus() == AccountStatus.ACTIVE;
    }

    @Override
    public @Nullable String getPassword() {
        return baseUser.getHashedPassword();
    }

    @Override
    public String getUsername() {
        return baseUser.getEmail();
    }

    public UserRole getRole() {
        return baseUser.getRole();
    }

    public UUID getId() {
        return baseUser.getId();
    }
    
}
