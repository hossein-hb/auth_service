package com.traazu.auth_service.security;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.traazu.auth_service.domain.entities.BaseUser;

public class CustomUserDetails implements UserDetails {

    private final BaseUser baseUser;

    public CustomUserDetails(BaseUser baseUser) {
        this.baseUser = baseUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + baseUser.getRole().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return baseUser.getHashedPassword();
    }

    @Override
    public String getUsername() {
        return baseUser.getEmail();
    }
    
}
