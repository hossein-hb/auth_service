package com.traazu.auth_service.services.auth;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.entities.BaseUser;
import com.traazu.auth_service.repositories.StaffRepository;
import com.traazu.auth_service.repositories.UserRepository;
import com.traazu.auth_service.security.CustomUserDetails;
import com.traazu.auth_service.security.JwtUtil;
import com.traazu.auth_service.services.auth.exceptions.InvalidRefreshTokenException;
import com.traazu.auth_service.services.auth.exceptions.UserNotFoundException;

@Service 
public class AccessTokenService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AccessTokenService(UserRepository userRepository, StaffRepository staffRepository, 
            JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }
    
    public AuthResponse refresh(String refreshToken) {

        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException(
                "Refresh token is invalid or expired"
            );
        }

        UUID baseUserId = jwtUtil.extractId(refreshToken);

        BaseUser baseUser = userRepository.findById(baseUserId)
                .map(user -> (BaseUser) user)
                .or(() -> staffRepository.findById(baseUserId)
                        .map(staff -> (BaseUser) staff)
                )
                .orElseThrow(() -> new UserNotFoundException(
                    "Refresh token failed. User not found with id: " + baseUserId
                ));

        CustomUserDetails userDetails = new CustomUserDetails(baseUser);
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        return new AuthResponse(accessToken);
    }
    
}
