package com.traazu.auth_service.services.auth.log_in;

import java.time.Duration;
import java.util.Collections;

import org.springframework.data.redis.core.StringRedisTemplate;
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
import com.traazu.auth_service.services.auth.exceptions.TooManyRequestsException;
import com.traazu.auth_service.services.auth.exceptions.UserNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogInService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redis;

    private static final Duration IP_COOLDOWN_TTL = Duration.ofMinutes(5);
    private static final Duration USERNAME_COOLDOWN_TTL = Duration.ofMinutes(5);
    private static final Long MAX_ATTEMPTS = 20L;

    public LogInService(UserRepository userRepository, StaffRepository staffRepository, PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil, RefreshTokenService refreshTokenService, StringRedisTemplate redis) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.redis = redis;
    }

    public AuthResponse logIn(LogInRequest request) {

        checkIpRateLimit(request.ipAddress());

        String userKey = LogInRedisPrefixes.USERNAME_RATE_LIMIT + request.username();
        String attemptsStr = redis.opsForValue().get(userKey);
        if (attemptsStr != null && Long.parseLong(attemptsStr) >= MAX_ATTEMPTS) {
            throw new TooManyRequestsException("Your account is temporarily locked due to too many failed attempts.");
        }

        BaseUser baseUser = request.role() == UserRole.USER ?
                                userRepository.findByEmail(request.username()).orElse(null):
                                staffRepository.findByEmail(request.username()).orElse(null);

        if (baseUser == null || !passwordEncoder.matches(request.password(), baseUser.getHashedPassword())) {
            increaseUsernameRateLimit(request.username());
            throw new UserNotFoundException("The username or password is incorrect!");
        }

        resetUsernameAttempts(request.username());

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

    public void increaseUsernameRateLimit(String username) {

        String userKey = LogInRedisPrefixes.USERNAME_RATE_LIMIT + username;

        Long result = redis.execute(
            LogInScripts.INCR_USERNAME_RATE_LIMIT,
            Collections.singletonList(userKey),
            String.valueOf(USERNAME_COOLDOWN_TTL.toMillis())
        );

        if (result == null) {
            log.error("Username rate limit increaser script returned null for user={}", username);
            throw new TooManyRequestsException("Username Rate limit check failed");
        } else if (result != 1) {
            log.error("Username rate limit increaser script returned invalid output for user={}", username);
            throw new TooManyRequestsException("Invalid output!");
        }

    }

    public void checkIpRateLimit(String ipAddress) {

        String ipKey = LogInRedisPrefixes.IP_RATE_LIMIT + ipAddress;

        Long result = redis.execute(
            LogInScripts.RATE_LIMIT_CHECKER,
            Collections.singletonList(ipKey),
            String.valueOf(IP_COOLDOWN_TTL.toMillis()),
            String.valueOf(MAX_ATTEMPTS)
        );

        if (result == null) {
            log.error("Rate limit script returned null for ip={}", ipAddress);
            throw new TooManyRequestsException("Rate limit check failed");
        }
        if (result == 0) {
            throw new TooManyRequestsException("Ip blocked!");
        }
        if (result != 1) {
            log.error("Rate limit script returned invalid output for ip={}", ipAddress);
            throw new TooManyRequestsException("Invalid output!");
        }

    }

    public void resetUsernameAttempts(String username) {
        String userKey = LogInRedisPrefixes.USERNAME_RATE_LIMIT + username;
        redis.delete(userKey);
    }
    
}
