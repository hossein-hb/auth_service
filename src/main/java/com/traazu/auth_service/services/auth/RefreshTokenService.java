package com.traazu.auth_service.services.auth;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.traazu.auth_service.security.JwtUtil;

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

    public void saveRefreshToken(UUID userId, String token, String deviceId, String deviceName) {
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
            deviceId == null ? "" : deviceId,
            deviceName == null ? "" : deviceName
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
            local deviceId = ARGV[6]
            local deviceName = ARGV[7]

            local cutoff = tonumber(now) - ttl
            redis.call('ZREMRANGEBYSCORE', userKey, 0, cutoff)

            local tokenKey = tokenKeyPrefix .. jti
            redis.call('HMSET', tokenKey,
                'userId', userId,
                'deviceId', deviceId,
                'deviceName', deviceName,
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
        String redisKey = USER_TOKENS_PREFIX + userId;
        redis.opsForZSet().remove(redisKey, jti);
    }

    public void revokeAllSessions(UUID userId) {
        String redisKey = USER_TOKENS_PREFIX + userId;
        redis.delete(redisKey);
    }
}