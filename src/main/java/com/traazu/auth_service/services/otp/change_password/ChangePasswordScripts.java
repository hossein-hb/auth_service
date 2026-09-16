package com.traazu.auth_service.services.otp.change_password;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class ChangePasswordScripts {

    public static final  RedisScript<Long> OTP_SAVER = new DefaultRedisScript<>(
            "local locked = redis.call('SET', KEYS[1], '1', 'NX', 'PX', ARGV[1]) " +
            "if not locked then " +
            "    return 0 " +
            "end " +
            "redis.call('SET', KEYS[2], ARGV[2], 'PX', ARGV[3]) " +
            "redis.call('SET', KEYS[3], ARGV[4], 'PX', ARGV[3]) " +
            "return 1",
            Long.class
        );

    public static final RedisScript<String> OTP_CONSUMER = new DefaultRedisScript<>(
            "local stored_otp = redis.call('GET', KEYS[1]) " +
            "if stored_otp == false then " +
            "    return \"-1\" " +
            "end " +
            "if stored_otp ~= ARGV[1] then " +
            "    return \"0\" " +
            "end " +
            "local role = redis.call('GET', KEYS[2]) " +
            "redis.call('DEL', KEYS[1]) " +
            "redis.call('DEL', KEYS[2]) " +
            "if not role then " +
            "    return \"ROLE_NOT_FOUND\" " +
            "end " +
            "return role",
            String.class
        );

    public static final  RedisScript<String> TOKEN_CONSUMER = new DefaultRedisScript<>(
            "local stored = redis.call('GET', KEYS[1]) " +
            "if stored == false then " +
            "    return '' " +
            "end " +
            "local sep = string.find(stored, ':', 1, true) " +
            "if not sep then " +
            "    return '' " +
            "end " +
            "local stored_token = string.sub(stored, 1, sep - 1) " +
            "local stored_role = string.sub(stored, sep + 1) " +
            "if stored_token ~= ARGV[1] then " +
            "    return '' " +
            "end " +
            "redis.call('DEL', KEYS[1]) " +
            "return stored_role",
            String.class
        );

}
