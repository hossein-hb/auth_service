package com.traazu.auth_service.services.auth;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.domain.dtos.auth.ClientDeviceInfo;
import com.traazu.auth_service.security.JwtUtil;

import jakarta.validation.constraints.NotNull;

@Service
public class RefreshTokenService {
    
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;

    private static final String USER_TOKENS_PREFIX = "refresh:user:";
    private static final String TOKEN_PREFIX = "refresh:token:";
    private static final int MAX_ACTIVE_DEVICES = 4;

    private final Duration refreshTokenExpiration;
    private final RedisScript<String> saveTokenScript;

    public RefreshTokenService(
            JwtUtil jwtUtil,
            StringRedisTemplate redis,
            @Value("${refresh.token.expiration}") Duration refreshTokenExpiration) {

        this.jwtUtil = jwtUtil;
        this.redis = redis;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.saveTokenScript = new DefaultRedisScript<>(getLuaScriptText(), String.class);
    }

    public void saveRefreshToken(UUID userId, String token, String ipAddress, 
            @NotNull(message = "ClientDeviceInfo cannot be null") ClientDeviceInfo clientInfo) {

        String jti = jwtUtil.extractJwtId(token);
        long now = System.currentTimeMillis();
        String userKey = USER_TOKENS_PREFIX + userId;

        redis.execute(
            saveTokenScript,
            List.of(userKey, TOKEN_PREFIX),
            jti,
            String.valueOf(now),
            String.valueOf(refreshTokenExpiration.toMillis()),
            String.valueOf(MAX_ACTIVE_DEVICES),
            userId.toString(),
            clientInfo.os() == null ? "Unknown" : clientInfo.os(),
            clientInfo.deviceType() == null ? "Unknown" : clientInfo.deviceType(),
            clientInfo.browser() == null ? "Unknown" : clientInfo.browser(),
            ipAddress == null ? "Unknown" : ipAddress
        );
    }

    private String getLuaScriptText() {
        return """
            local userKey = KEYS[1]
            local tokenKeyPrefix = KEYS[2]
            local jti = ARGV[1]
            local now = ARGV[2]
            local ttl = tonumber(ARGV[3])
            local maxDevices = tonumber(ARGV[4])
            local userId = ARGV[5]
            local os = ARGV[6]
            local deviceType = ARGV[7]
            local browser = ARGV[8]
            local ipAddress = ARGV[9]

            local cutoff = tonumber(now) - ttl
            redis.call('ZREMRANGEBYSCORE', userKey, 0, cutoff)

            local tokenKey = tokenKeyPrefix .. jti
            redis.call('HSET', tokenKey,
                'userId', userId,
                'os', os,
                'deviceType', deviceType,
                'browser', browser,
                'ipAddress', ipAddress,
                'createdAt', now
            )
            redis.call('PEXPIRE', tokenKey, ttl)

            redis.call('ZADD', userKey, now, jti)
            redis.call('PEXPIRE', userKey, ttl)

            local size = redis.call('ZCARD', userKey)
            if size > maxDevices then
                local excess = size - maxDevices
                local oldestJtis = redis.call('ZRANGE', userKey, 0, excess - 1)
                for _, oldJti in ipairs(oldestJtis) do
                    redis.call('DEL', tokenKeyPrefix .. oldJti)
                end
                redis.call('ZREMRANGEBYRANK', userKey, 0, excess - 1)
            end
            return "OK"
            """;
    }

    public boolean isSessionActive(UUID userId, String jti) {
        String redisKey = USER_TOKENS_PREFIX + userId;
        Double score = redis.opsForZSet().score(redisKey, jti);
        return score != null;
    }

    public void revokeRefreshToken(UUID userId, String jti) {
        String userKey = USER_TOKENS_PREFIX + userId;
        redis.opsForZSet().remove(userKey, jti);
        redis.delete(TOKEN_PREFIX + jti);
    }

    public void revokeAllSessions(UUID userId) {
        String userKey = USER_TOKENS_PREFIX + userId;
        Set<String> jtis = redis.opsForZSet().range(userKey, 0, -1);
        if (jtis != null && !jtis.isEmpty()) {
            List<String> tokenKeys = jtis.stream()
                    .map(jti -> TOKEN_PREFIX + jti)
                    .toList();
            redis.delete(tokenKeys);
        }
        redis.delete(userKey);
    }

    public boolean validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }

        try {
            UUID userId = jwtUtil.extractId(refreshToken);
            String jti = jwtUtil.extractJwtId(refreshToken);

            if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
                return false;
            }

            return isSessionActive(userId, jti);

        } catch (Exception e) {
            return false;
        }
    }

}