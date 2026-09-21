package com.traazu.auth_service.redis.prefixes;

import java.time.Duration;

public interface RedisPrefixes {

    String getLockPrefix();

    String getLockEnterOtpPrefix();

    String getOtpPrefix();

    String getTokenPrefix();

    String getAttemptEnterOtpPrefix();

    int getMaxAttempts();

    Duration getAttemptTTL();

    Duration getLockEnterOtpTTL();

    Duration getLockTTL();

    Duration getOtpTTL();

    Duration getTokenTTL();

}
