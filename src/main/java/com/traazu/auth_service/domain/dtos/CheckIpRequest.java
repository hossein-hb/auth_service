package com.traazu.auth_service.domain.dtos;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckIpRequest(

    @NotNull(message = "The emial redis not been defined.")
    StringRedisTemplate redis,

    @NotBlank(message = "The redisIpKey has not been defined.")
    String redisIpKey,

    @NotNull(message = "The ipCooldownTTL has not been defined.")
    Duration ipCooldownTTL,

    @NotNull(message = "The maxAttempts has not been defined.")
    Long maxAttempts

) {}
