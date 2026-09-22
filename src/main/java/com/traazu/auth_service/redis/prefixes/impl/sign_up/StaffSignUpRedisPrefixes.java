package com.traazu.auth_service.redis.prefixes.impl.sign_up;

public class StaffSignUpRedisPrefixes extends SignUpRedisPrefixes {

    @Override
    public String getLockPrefix() {
        return "staff:signup:lock:";
    }

    @Override
    public String getLockEnterOtpPrefix() {
        return "staff:signup:lock:enter:otp:";
    }

    @Override
    public String getOtpPrefix() {
        return "staff:signup:otp:";
    }

    @Override
    public String getTokenPrefix() {
        return "staff:signup:token:";
    }

    @Override
    public String getAttemptEnterOtpPrefix() {
        return "staff:signup:attempt:enter:otp:";
    }
    
}
