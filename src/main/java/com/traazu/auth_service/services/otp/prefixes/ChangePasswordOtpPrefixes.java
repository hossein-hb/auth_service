package com.traazu.auth_service.services.otp.prefixes;

import java.time.Duration;

public class ChangePasswordOtpPrefixes implements OtpPrefixes {

    @Override
    public String getLockPrefix() {
        return "change:password:lock:";
    }

    @Override
    public String getLockEnterOtpPrefix() {
        return "change:password:lock:enter:otp:";
    }

    @Override
    public String getOtpPrefix() {
        return "change:password:otp:";
    }

    @Override
    public String getTokenPrefix() {
        return "change:password:token";
    }

    @Override
    public String getAttemptEnterOtpPrefix() {
        return "change:password:attempt:enter:otp:";
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
