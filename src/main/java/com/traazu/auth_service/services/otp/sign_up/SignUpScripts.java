package com.traazu.auth_service.services.otp.sign_up;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class SignUpScripts {

    public static final  RedisScript<Long> OTP_SAVER = new DefaultRedisScript<>(
            "local locked = redis.call('SET', KEYS[1], '1', 'NX', 'PX', ARGV[1]) " +
            "if not locked then " +
            "    return 0 " +
            "end " +
            "redis.call('SET', KEYS[2], ARGV[2], 'PX', ARGV[3]) " +
            "return 1",
            Long.class
        );

    public static final  RedisScript<Long> OTP_CONSUMER = new DefaultRedisScript<>(
            "local stored_otp = redis.call('GET', KEYS[1]) " +
            "if stored_otp == false then " +
            "    return -1 " +
            "elseif stored_otp == ARGV[1] then " +
            "    redis.call('DEL', KEYS[1]) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end",
            Long.class
        );

    public static final  RedisScript<Long> TOKEN_CONSUMER = new DefaultRedisScript<>(
            "local stored_token = redis.call('GET', KEYS[1]) " +
            "if stored_token == false then " +
            "    return -1 " +
            "end " +
            "if stored_token ~= ARGV[1] then " +
            "    return 0 " +
            "end " +
            "redis.call('DEL', KEYS[1]) " +
            "return 1",
            Long.class
        );
    
}
