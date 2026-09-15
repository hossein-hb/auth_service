package com.traazu.auth_service.services.otp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.services.otp.prefixes.OtpPrefixes;

@Service
public class SignUpOtpService extends AbstractOtpService {

    private final OtpPrefixes prefixes;

    public SignUpOtpService(LockOtpService lockOtpService,
                            AttemptOtpService attemptOtpService,
                            StringRedisTemplate redisTemplate,
                            @Qualifier("signUpOtpPrefixes") OtpPrefixes prefixes) {
        super(lockOtpService, attemptOtpService, redisTemplate);
        this.prefixes = prefixes;
    }

    @Override
    protected OtpPrefixes getPrefixes() {
        return prefixes;
    }
    
}