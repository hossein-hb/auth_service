package com.traazu.auth_service.services.otp.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OtpTarget {

    SIGNUP_LOCK("signup:lock:"),
    SIGNUP_LOCK_ENTER_OTP("signup:lock:enter:otp:"),
    CHANGE_PASSWORD_LOCK("change:password:lock:"),
    CHANGE_PASSWORD_LOCK_ENTER_OTP("change:password:lock:enter:otp:"),
    SIGNUP_OTP("signup:otp:"),
    SIGNUP_TOKEN("signup:token:"),
    SIGNUP_ATTEMPT_ENTER_OTP("signup:attempt:enter:otp:"),
    CHANGE_PASSWORD_ATTEMPT_ENTER_OTP("change:password:attempt:enter:otp:");

    private final String prefix;
}