package com.traazu.auth_service.redis.prefixes.impl.sign_up;

import java.time.Duration;

import com.traazu.auth_service.redis.prefixes.RedisPrefixes;

public abstract class SignUpRedisPrefixes implements RedisPrefixes {

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
