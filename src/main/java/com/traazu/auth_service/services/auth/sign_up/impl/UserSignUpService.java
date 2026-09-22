package com.traazu.auth_service.services.auth.sign_up.impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.dtos.auth.sign_up.impl.UserSignUpInfos;
import com.traazu.auth_service.domain.entities.User;
import com.traazu.auth_service.domain.factory.UserFactory;
import com.traazu.auth_service.redis.sign_up.SignUpOtpService;
import com.traazu.auth_service.repositories.BaseUserRepository;
import com.traazu.auth_service.security.CustomUserDetails;
import com.traazu.auth_service.security.JwtUtil;
import com.traazu.auth_service.services.auth.RefreshTokenService;
import com.traazu.auth_service.services.auth.sign_up.SignUpService;

@Service
public class UserSignUpService extends SignUpService<User, UserSignUpInfos> {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    private static final Duration IP_COOLDOWN_TTL = Duration.ofMinutes(5);
    private static final Long MAX_ATTEMPTS = 20L;
    private static final String IP_RATE_LIMIT = "ratelimit:user:signup:ip:";




    public UserSignUpService(UserFactory userFactory,
            BaseUserRepository<User, UUID> baseUserRepository, StringRedisTemplate redis, 
            JwtUtil jwtUtil, RefreshTokenService refreshTokenService, 
            @Qualifier("userSignUpOtpService") SignUpOtpService signUpOtpService) {

        super(userFactory, baseUserRepository, signUpOtpService, redis, 
                IP_COOLDOWN_TTL, MAX_ATTEMPTS, IP_RATE_LIMIT);
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;

    }

    public AuthResponse completeSignUp(UserSignUpInfos request) {
        User user = completeInformation(request);
        
        String accessJwtToken = jwtUtil.generateAccessToken(new CustomUserDetails(user));
        String refreshJwtToken = jwtUtil.generateRefreshToken(user.getId());
        
        String deviceId = (request.getDeviceInfo() != null) ? request.getDeviceInfo().deviceId() : null;
        String deviceName = (request.getDeviceInfo() != null) ? request.getDeviceInfo().deviceName() : "Unknown Device";

        refreshTokenService.saveRefreshToken(user.getId(), refreshJwtToken, deviceId, deviceName);

        return new AuthResponse(accessJwtToken, refreshJwtToken);
    }
    
}
