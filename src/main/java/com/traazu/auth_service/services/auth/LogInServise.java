package com.traazu.auth_service.services.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.dtos.auth.LogInRequest;
import com.traazu.auth_service.domain.entities.BaseUser;
import com.traazu.auth_service.domain.enums.AccountStatus;
import com.traazu.auth_service.domain.enums.UserRole;
import com.traazu.auth_service.repositories.StaffRepository;
import com.traazu.auth_service.repositories.UserRepository;
import com.traazu.auth_service.security.JwtUtil;
import com.traazu.auth_service.services.auth.exceptions.AccountLockedException;
import com.traazu.auth_service.services.auth.exceptions.InvalidPasswordException;
import com.traazu.auth_service.services.auth.exceptions.UserNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LogInServise {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse logIn(LogInRequest request) {

        BaseUser baseUser;
        if (request.role() == UserRole.USER) {
            baseUser = userRepository.findByEmail(request.username()).orElse(null);
        } else {
            baseUser = staffRepository.findByEmail(request.username()).orElse(null);
        }

        if (baseUser == null) {
            throw new UserNotFoundException("The username or password is incorrect!");
        }
        if (!passwordEncoder.matches(request.password(), baseUser.getHashedPassword())) {
            throw new InvalidPasswordException("The username or password is incorrect!");
        }
        if (AccountStatus.BANNED == baseUser.getAccountStatus()) {
            throw new AccountLockedException("Your account has been banned. Please contact support.");
        }
        
        String jwtToken = jwtUtil.generateToken(request.username(), request.role());

        return new AuthResponse(jwtToken);
        
    }
    
}
