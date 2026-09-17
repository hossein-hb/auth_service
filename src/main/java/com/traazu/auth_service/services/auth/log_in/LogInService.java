package com.traazu.auth_service.services.auth.log_in;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.dtos.auth.LogInRequest;
import com.traazu.auth_service.domain.entities.BaseUser;
import com.traazu.auth_service.domain.enums.AccountStatus;
import com.traazu.auth_service.domain.enums.UserRole;
import com.traazu.auth_service.repositories.StaffRepository;
import com.traazu.auth_service.repositories.UserRepository;
import com.traazu.auth_service.security.CustomUserDetails;
import com.traazu.auth_service.security.JwtUtil;
import com.traazu.auth_service.services.auth.RefreshTokenService;
import com.traazu.auth_service.services.auth.exceptions.AccountLockedException;
import com.traazu.auth_service.services.auth.exceptions.InvalidPasswordException;
import com.traazu.auth_service.services.auth.exceptions.RoleNotFoundException;
import com.traazu.auth_service.services.auth.exceptions.RoleNotSetException;
import com.traazu.auth_service.services.auth.exceptions.UserNotFoundException;

@Service
public class LogInService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public LogInService(UserRepository userRepository, StaffRepository staffRepository, PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse logIn(LogInRequest request) {

        if (request.role() == null) {
            throw new RoleNotSetException("The role is not set!");
        }

        if (request.role() != UserRole.USER && request.role() != UserRole.SUPPORT && request.role() != UserRole.ADMIN) {
            throw new RoleNotFoundException(String.format("There is no %s role!", request.role().toString()));
        }

        BaseUser baseUser = request.role() == UserRole.USER ?
                                userRepository.findByEmail(request.username()).orElse(null):
                                staffRepository.findByEmail(request.username()).orElse(null);

        if (baseUser == null) {
            throw new UserNotFoundException("The username or password is incorrect!");
        }
        if (!passwordEncoder.matches(request.password(), baseUser.getHashedPassword())) {
            throw new InvalidPasswordException("The username or password is incorrect!");
        }
        if (AccountStatus.BANNED == baseUser.getAccountStatus()) {
            throw new AccountLockedException("Your account has been banned. Please contact support.");
        }
        
        String accessJwtToken = jwtUtil.generateAccessToken(new CustomUserDetails(baseUser));
        String refreshJwtToken = jwtUtil.generateRefreshToken(baseUser.getId());
        
        String deviceId = (request.deviceInfo() != null) ? request.deviceInfo().deviceId() : null;
        String deviceName = (request.deviceInfo() != null) ? request.deviceInfo().deviceName() : "Unknown Device";

        refreshTokenService.saveRefreshToken(baseUser.getId(), refreshJwtToken, deviceId, deviceName);

        return new AuthResponse(accessJwtToken, refreshJwtToken);
        
    }
    
}
