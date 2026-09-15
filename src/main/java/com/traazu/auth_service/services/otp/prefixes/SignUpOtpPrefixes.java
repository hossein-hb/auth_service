package com.traazu.auth_service.services.otp.prefixes;

import java.time.Duration;

public class SignUpOtpPrefixes implements OtpPrefixes {

    @Override
    public String getLockPrefix() {
        return "signup:lock:";
    }

    @Override
    public String getLockEnterOtpPrefix() {
        return "signup:lock:enter:otp:";
    }

    @Override
    public String getOtpPrefix() {
        return "signup:otp:";
    }

    @Override
    public String getTokenPrefix() {
        return "signup:token:";
    }

    @Override
    public String getAttemptEnterOtpPrefix() {
        return "signup:attempt:enter:otp:";
    }

    @Override
    public int getMaxAttempts() {
        return 5;
    }

    @Override
    public Duration getAttemptTTL() {
        return Duration.ofMinutes(5);
    }

    @Override
    public Duration getLockEnterOtpTTL() {
        return Duration.ofMinutes(15);
    }

    @Override
    public Duration getLockTTL() {
        return Duration.ofMinutes(2);
    }

    @Override
    public Duration getOtpTTL() {
        return Duration.ofMinutes(2);
    }

    @Override
    public Duration getTokenTTL() {
        return Duration.ofMinutes(10);
    }
    
}
