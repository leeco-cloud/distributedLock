package com.leeco.cloud.lock.redis.redislock.lock;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.StaticScriptSource;

/**
 * redis分布式锁lua脚本
 * @author liuqiang-068
 */
public class RedisLockLuaScript {

    /**
     * 不可重入
     * 获取lua脚本定义
     * 初始化锁的过期时间时5秒
     * 避免启动时间占用过久
     */
    public static DefaultRedisScript<Boolean> getDefaultRedisScript() {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
        script.setScriptSource(new StaticScriptSource(
                "if (redis.call('exists', KEYS[1]) == 0) then " +
                        "redis.call('set', KEYS[1], ARGV[1]); " +
                        "redis.call('expire', KEYS[1], 5); " +
                        "return true; " +
                        "end; " +
                        "return false"));
        script.setResultType(Boolean.class);
        return script;
    }

    /**
     * 不可重入
     * 获取lua脚本定义
     * 如果是当前节点  则释放锁
     */
    public static DefaultRedisScript<Boolean> getDeleteRedisScript() {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
        script.setScriptSource(new StaticScriptSource(
                "if (redis.call('exists', KEYS[1]) == 1) then " +
                        "if (redis.call('get', KEYS[1]) == ARGV[1]) then " +
                        "redis.call('del', KEYS[1]);" +
                        "return true;" +
                        "end " +
                        "end " +
                        "return false;"));
        script.setResultType(Boolean.class);
        return script;
    }


    /**
     * 可重入
     * 获取lua脚本定义
     * 初始化锁的过期时间时5秒
     * 避免启动时间占用过久
     */
    public static DefaultRedisScript<Long> getReentrantRedisScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new StaticScriptSource("if (redis.call('exists', KEYS[1]) == 0) then" +
                "    redis.call('hset', KEYS[1], ARGV[1], 1);" +
                "    redis.call('expire', KEYS[1], ARGV[2]);" +
                "    return 1;" +
                "end;" +
                "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then" +
                "    redis.call('hincrby', KEYS[1], ARGV[1], 1);" +
                "    redis.call('expire', KEYS[1], ARGV[2]);" +
                "    return 2;" +
                "end;" +
                "return -1;"));
        script.setResultType(Long.class);
        return script;
    }

    /**
     * 可重入
     * 获取lua脚本定义
     * 如果是当前节点  则释放锁
     */
    public static DefaultRedisScript<Long> getReentrantDeleteRedisScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new StaticScriptSource("if (redis.call('exists', KEYS[1]) == 0) then" +
                "    return 1;" +
                "end;" +
                "if (tonumber(redis.call('hget', KEYS[1], ARGV[1])) == 1) then" +
                "    redis.call('del', KEYS[1]);" +
                "    return 1;" +
                "end;" +
                "if (tonumber(redis.call('hget', KEYS[1], ARGV[1])) > 1) then" +
                "    redis.call('hincrby', KEYS[1], ARGV[1], -1);" +
                "    return 2;" +
                "end;" +
                "return -1;"));
        script.setResultType(Long.class);
        return script;
    }

}
