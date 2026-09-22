package com.traazu.auth_service.services.auth.sign_up.impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.dtos.auth.sign_up.impl.StaffSignUpInfos;
import com.traazu.auth_service.domain.entities.Staff;
import com.traazu.auth_service.domain.factory.StaffFactory;
import com.traazu.auth_service.redis.sign_up.SignUpOtpService;
import com.traazu.auth_service.repositories.BaseUserRepository;
import com.traazu.auth_service.services.auth.sign_up.SignUpService;

@Service
public class StaffSignUpService extends SignUpService<Staff, StaffSignUpInfos> {

    private static final Duration IP_COOLDOWN_TTL = Duration.ofMinutes(5);
    private static final Long MAX_ATTEMPTS = 20L;
    private static final String IP_RATE_LIMIT = "ratelimit:staff:signup:ip:";

    public StaffSignUpService(StaffFactory staffFactory,
            BaseUserRepository<Staff, UUID> baseUserRepository, StringRedisTemplate redis,
            @Qualifier("staffSignUpOtpService")  SignUpOtpService signUpOtpService) {

        super(staffFactory, baseUserRepository, signUpOtpService, redis,
                IP_COOLDOWN_TTL, MAX_ATTEMPTS, IP_RATE_LIMIT);

    }
    
}
