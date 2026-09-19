package com.traazu.auth_service.services.auth.log_in;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class LogInScripts {

    public static final RedisScript<Long> INCR_USERNAME_RATE_LIMIT = new DefaultRedisScript<>(
        "local new_user = redis.call('INCR', KEYS[1]) " +
        "if new_user == 1 then " +
        "   redis.call('PEXPIRE', KEYS[1], ARGV[1]) " +
        "   return 1 " +
        "end ",
        Long.class
    );

}
