package com.traazu.auth_service.services.auth;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.traazu.auth_service.domain.dtos.ChangePasswordRequest;
import com.traazu.auth_service.domain.dtos.ForgetPasswordRequest;
import com.traazu.auth_service.domain.dtos.auth.OtpVerificationRequest;
import com.traazu.auth_service.domain.entities.Staff;
import com.traazu.auth_service.domain.entities.User;
import com.traazu.auth_service.domain.enums.UserRole;
import com.traazu.auth_service.repositories.StaffRepository;
import com.traazu.auth_service.repositories.UserRepository;
import com.traazu.auth_service.services.auth.exceptions.PasswordMismatchException;
import com.traazu.auth_service.services.auth.exceptions.RoleNotFoundException;
import com.traazu.auth_service.services.auth.exceptions.RoleNotSetException;
import com.traazu.auth_service.services.auth.exceptions.UserNotFoundException;
import com.traazu.auth_service.services.auth.exceptions.WeakPasswordException;
import com.traazu.auth_service.services.otp.change_password.ChangePasswordOtpService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ForgetPasswordService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChangePasswordOtpService changePasswordOtpService;

    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()_\\-]).{8,72}$");

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));
    }

    private Staff resolveStaff(String email) {
        return staffRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));
    }

    private void assertUserExists(String email, UserRole role) {
        switch (role) {
            case USER -> {
                if (!userRepository.existsByEmail(email)) {
                    throw new UserNotFoundException("User not found!");
                }
            }
            case ADMIN, SUPPORT -> {
                if (!staffRepository.existsByEmail(email)) {
                    throw new UserNotFoundException("User not found!");
                }
            }
            default -> throw new RoleNotFoundException("Invalid role!");
        }
    }

    private void applyNewPassword(String email, UserRole role, String hashedPassword) {
        switch (role) {
            case USER -> {
                User user = resolveUser(email);
                user.setHashedPassword(hashedPassword);
                userRepository.save(user);
            }
            case ADMIN, SUPPORT -> {
                Staff staff = resolveStaff(email);
                staff.setHashedPassword(hashedPassword);
                staffRepository.save(staff);
            }
            default -> throw new RoleNotFoundException("Invalid role!");
        }
    }

    public String sendVerificationOtpCode(ForgetPasswordRequest request) {
        if (request.userRole() == null) {
            throw new RoleNotSetException("The role is not set!");
        }

        assertUserExists(request.email(), request.userRole());
        return changePasswordOtpService.generateAndSaveOtp(request.email(), request.userRole());
    }

    public String verifyOtpAndIssueChangePasswordToken(OtpVerificationRequest request) {
        return changePasswordOtpService.verifyAndConsumeOtp(request.email(), request.otpCode());
    }

    @Transactional
    public void setNewPassword(ChangePasswordRequest request) {

        if (!PASSWORD_PATTERN.matcher(request.newPassword()).matches()) {
            throw new WeakPasswordException(
                "Password must contain at least 8 characters (max 72), " +
                "one uppercase, one lowercase, one number and one special character."
            );
        }

        if (!request.newPassword().equals(request.repeatNewPassword())) {
            throw new PasswordMismatchException("Password does not match the repeated password!");
        }

        UserRole role = changePasswordOtpService
                .verifyAndConsumeTokenAndReturnRole(request.email(), request.token());

        String hashedPassword = passwordEncoder.encode(request.newPassword());
        applyNewPassword(request.email(), role, hashedPassword);
    }
}