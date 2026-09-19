package com.traazu.auth_service.services.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.dtos.CheckIpRequest;
import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.dtos.auth.OtpVerificationRequest;
import com.traazu.auth_service.domain.dtos.auth.UserSignUpInfos;
import com.traazu.auth_service.domain.dtos.auth.SignUpRequest;
import com.traazu.auth_service.domain.entities.User;
import com.traazu.auth_service.domain.enums.AccountStatus;
import com.traazu.auth_service.repositories.UserRepository;
import com.traazu.auth_service.security.CustomUserDetails;
import com.traazu.auth_service.security.JwtUtil;
import com.traazu.auth_service.services.auth.exceptions.DuplicateEmailException;
import com.traazu.auth_service.services.auth.exceptions.InvalidRegistrationTokenException;
import com.traazu.auth_service.services.auth.exceptions.PasswordMismatchException;
import com.traazu.auth_service.services.auth.exceptions.WeakPasswordException;
import com.traazu.auth_service.services.otp.sign_up.SignUpOtpService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignUpOtpService signUpOtpService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redis;

    private static final Duration IP_COOLDOWN_TTL = Duration.ofMinutes(5);
    private static final Long MAX_ATTEMPTS = 20L;
    private static final String IP_RATE_LIMIT = "ratelimit:signup:ip:";

    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$");

    private User createUser(UserSignUpInfos request) {

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setHashedPassword(hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setAccountStatus(AccountStatus.ACTIVE);

        return user;

    }

    public String sendVerifivationCodeForSignUp(SignUpRequest request) {

        checkIpRateLimit(request.ipAddress());

        if (userRepository.existsByEmail(request.emial())) {
            throw new DuplicateEmailException(
                "Email '" + request.emial() + "' is already registered"
            );
        }

        return signUpOtpService.generateAndSaveOtp(request.emial());
    }

    public String checkVerifivationCodeForSignUp(OtpVerificationRequest request) {
        return signUpOtpService.verifyAndConsumeOtp(request.email(), request.otpCode());
    }

    public AuthResponse completeInformaion(UserSignUpInfos request) {

        if (!signUpOtpService.verifyAndConsumeToken(request.email(), request.token())) {
            throw new InvalidRegistrationTokenException("Invalid token!");
        }

        if (!request.password().equals(request.repeatPassword())) {
            throw new PasswordMismatchException("Password is not match with repeate password!");
        }

        if (!PASSWORD_PATTERN.matcher(request.password()).matches()) {
            throw new WeakPasswordException(
                "Password must contain at least 8 characters, " +
                "one uppercase, one lowercase, one number and one special character"
            );
        }

        User user = createUser(request);
        userRepository.save(user);
        
        String accessJwtToken = jwtUtil.generateAccessToken(new CustomUserDetails(user));
        String refreshJwtToken = jwtUtil.generateRefreshToken(user.getId());
        
        String deviceId = (request.deviceInfo() != null) ? request.deviceInfo().deviceId() : null;
        String deviceName = (request.deviceInfo() != null) ? request.deviceInfo().deviceName() : "Unknown Device";

        refreshTokenService.saveRefreshToken(user.getId(), refreshJwtToken, deviceId, deviceName);

        return new AuthResponse(accessJwtToken, refreshJwtToken);

    }

    private void checkIpRateLimit(String ipAddress) {
        String redisIpKey = IP_RATE_LIMIT + ipAddress;
        CheckIpRequest checkIpRequest = new CheckIpRequest(redis, redisIpKey, IP_COOLDOWN_TTL, MAX_ATTEMPTS);

        IpRateLimiter.checkIp(checkIpRequest);
    }
    
}
