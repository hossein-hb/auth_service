package com.traazu.auth_service.services.auth.oauth;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.traazu.auth_service.domain.dtos.auth.ClientDeviceInfo;
import com.traazu.auth_service.domain.dtos.auth.LogInResult;
import com.traazu.auth_service.domain.entities.User;
import com.traazu.auth_service.domain.enums.AccountStatus;
import com.traazu.auth_service.domain.enums.AuthProvider;
import com.traazu.auth_service.domain.enums.UserRole;
import com.traazu.auth_service.repositories.UserRepository;
import com.traazu.auth_service.security.CustomUserDetails;
import com.traazu.auth_service.security.JwtUtil;
import com.traazu.auth_service.services.auth.RefreshTokenService;
import com.traazu.auth_service.services.auth.exceptions.AccountLockedException;

@Service
public class GoogleOAuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public GoogleOAuthService(
            GoogleTokenVerifier googleTokenVerifier,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService) {

        this.googleTokenVerifier = googleTokenVerifier;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public LogInResult authenticate(
            String idToken,
            String ipAddress,
            ClientDeviceInfo clientInfo) {

        Jwt googleToken = googleTokenVerifier.verify(idToken);

        String googleSubject = googleToken.getSubject();
        String email = googleToken.getClaimAsString("email");

        User user = userRepository
                .findByGoogleSubject(googleSubject)
                .orElse(null);

        if (user != null) {
            checkAccountStatus(user);
            return createSession(user, ipAddress, clientInfo);
        }

        user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user != null) {

            checkAccountStatus(user);

            throw new IllegalStateException(
                    "An account with this email already exists. " +
                    "Please log in using your password first and link Google from your account settings."
            );
        }

        user = createGoogleUser(googleToken);

        user = userRepository.save(user);

        return createSession(user, ipAddress, clientInfo);
    }

    private User createGoogleUser(Jwt googleToken) {

        String email = googleToken.getClaimAsString("email");

        String firstName =
                googleToken.getClaimAsString("given_name");

        String lastName =
                googleToken.getClaimAsString("family_name");

        String fullName =
                googleToken.getClaimAsString("name");

        if (firstName == null || firstName.isBlank()) {
            firstName = extractFirstName(fullName);
        }

        if (lastName == null || lastName.isBlank()) {
            lastName = extractLastName(fullName);
        }

        firstName = normalizeName(firstName, "Google");
        lastName = normalizeName(lastName, "User");

        User user = new User();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setHashedPassword(null);
        user.setRole(UserRole.USER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setGoogleSubject(googleToken.getSubject());

        return user;
    }

    private LogInResult createSession(
            User user,
            String ipAddress,
            ClientDeviceInfo clientInfo) {

        String accessToken =
                jwtUtil.generateAccessToken(
                        new CustomUserDetails(user)
                );

        String refreshToken =
                jwtUtil.generateRefreshToken(user.getId());

        refreshTokenService.saveRefreshToken(
                user.getId(),
                refreshToken,
                ipAddress,
                clientInfo
        );

        return new LogInResult(
                accessToken,
                refreshToken
        );
    }

    private void checkAccountStatus(User user) {

        if (user.getAccountStatus() == AccountStatus.BANNED) {
            throw new AccountLockedException(
                    "Your account has been banned. Please contact support."
            );
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountLockedException(
                    "Your account is not active."
            );
        }
    }

    private String extractFirstName(String fullName) {

        if (fullName == null || fullName.isBlank()) {
            return "Google";
        }

        String[] parts = fullName.trim().split("\\s+");

        return parts[0];
    }

    private String extractLastName(String fullName) {

        if (fullName == null || fullName.isBlank()) {
            return "User";
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return "User";
        }

        return parts[parts.length - 1];
    }

    private String normalizeName(String value, String fallback) {

        if (value == null || value.isBlank()) {
            return fallback;
        }

        value = value.trim();

        if (value.length() > 20) {
            return value.substring(0, 20);
        }

        return value;
    }

}