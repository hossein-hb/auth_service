package com.traazu.auth_service.services.auth.log_in;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class LogInScripts {

    public static final RedisScript<Long> RATE_LIMIT_CHECKER = new DefaultRedisScript<>(
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

    public static final RedisScript<Long> INCR_USERNAME_RATE_LIMIT = new DefaultRedisScript<>(
        "local new_user = redis.call('INCR', KEYS[1]) " +
        "if new_user == 1 then " +
        "   redis.call('PEXPIRE', KEYS[1], ARGV[1]) " +
        "   return 1 " +
        "end ",
        Long.class
    );

}
