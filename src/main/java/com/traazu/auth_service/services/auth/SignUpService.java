package com.traazu.auth_service.services.auth;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.dtos.auth.AuthResponse;
import com.traazu.auth_service.domain.dtos.auth.OtpVerificationRequest;
import com.traazu.auth_service.domain.dtos.auth.UserSignUpRequest;
import com.traazu.auth_service.domain.entities.User;
import com.traazu.auth_service.domain.enums.AccountStatus;
import com.traazu.auth_service.domain.enums.UserRole;
import com.traazu.auth_service.repositories.UserRepository;
import com.traazu.auth_service.security.JwtUtil;
import com.traazu.auth_service.services.auth.exceptions.DuplicateEmailException;
import com.traazu.auth_service.services.auth.exceptions.InvalidRegistrationTokenException;
import com.traazu.auth_service.services.auth.exceptions.PasswordMismatchException;
import com.traazu.auth_service.services.auth.exceptions.WeakPasswordException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$");

    private User createUser(UserSignUpRequest request) {

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


    public void sendVerifivationCodeForSignUp(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(
                "Email '" + email + "' is already registered"
            );
        }

        otpService.generateAndSaveOtp(email);
    }

    public String checkVerifivationCodeForSignUp(OtpVerificationRequest request) {
        return otpService.verifyAndConsumeOtp(request.email(), request.otpCode());
    }

    public AuthResponse completeInformaion(UserSignUpRequest request) {

        if (!otpService.verifyAndConsumeToken(request.email(), request.token())) {
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
        
        String jwtToken = jwtUtil.generateToken(request.email(), UserRole.USER);
        return new AuthResponse(jwtToken);

    }
    
}
