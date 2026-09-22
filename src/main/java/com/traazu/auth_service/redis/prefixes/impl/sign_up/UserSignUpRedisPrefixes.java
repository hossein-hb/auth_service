package com.traazu.auth_service.redis.prefixes.impl.sign_up;


public class UserSignUpRedisPrefixes extends SignUpRedisPrefixes {

    @Override
    public String getLockPrefix() {
        return "user:signup:lock:";
    }

    @Override
    public String getLockEnterOtpPrefix() {
        return "user:signup:lock:enter:otp:";
    }

    @Override
    public String getOtpPrefix() {
        return "user:signup:otp:";
    }

    @Override
    public String getTokenPrefix() {
        return "user:signup:token:";
    }

    @Override
    public String getAttemptEnterOtpPrefix() {
        return "user:signup:attempt:enter:otp:";
    }
    
}
