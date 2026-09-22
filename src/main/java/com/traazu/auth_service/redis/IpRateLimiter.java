package com.traazu.auth_service.redis;

import java.util.Collections;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.traazu.auth_service.domain.dtos.CheckIpRequest;
import com.traazu.auth_service.services.auth.exceptions.TooManyRequestsException;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
public class IpRateLimiter {

    private static final RedisScript<Long> IP_RATE_LIMIT = new DefaultRedisScript<>(
        "local max_attempts = tonumber(ARGV[2]) " +

        "local trys_with_ip = redis.call('GET', KEYS[1]) " +
        "if trys_with_ip and tonumber(trys_with_ip) >= max_attempts then " +
        "   return 0 " +
        "end " +

        "local new_ip = redis.call('INCR', KEYS[1]) " +
        "if new_ip == 1 then " +
        "   redis.call('PEXPIRE', KEYS[1], ARGV[1]) " +
        "end " +

        "return 1 ",
        Long.class
    );

    public static void checkIp(CheckIpRequest request) {

        Long result = request.redis().execute(
            IP_RATE_LIMIT,
            Collections.singletonList(request.redisIpKey()),
            String.valueOf(request.ipCooldownTTL().toMillis()),
            String.valueOf(request.maxAttempts())
        );

        if (result == null) {
            log.error("Rate limit script returned null for ip={}", request.redisIpKey());
            throw new TooManyRequestsException("Rate limit check failed");
        }
        if (result == 0) {
            throw new TooManyRequestsException("Ip blocked!");
        }
        if (result != 1) {
            log.error("Rate limit script returned invalid output for ip={}", request.redisIpKey());
            throw new TooManyRequestsException("Invalid output!");
        }

    }
    
}
