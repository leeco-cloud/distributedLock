package com.leeco.cloud.lock.redis.server;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/27 19:09
 */
@SuppressWarnings("unchecked")
public class RefreshLock extends Thread{

    // 上的锁的redis中的key
    private final String lockKey;

    // 上锁的最大时间
    private final Long expireTime;

    // 总共允许续期几次
    private final Integer count;

    // 隔几秒刷新
    private final Long refreshTime;

    private final RedisTemplate redisTemplate;

    private final DefaultRedisScript<Boolean> refreshRedisScript;

    RefreshLock(String lockKey, Long expireTime, Integer count, Long refreshTime, RedisTemplate redisTemplate, DefaultRedisScript<Boolean> refreshRedisScript){
        this.lockKey = lockKey;
        this.expireTime = expireTime;
        this.count = count;
        this.refreshRedisScript = refreshRedisScript;
        this.refreshTime = refreshTime;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        int countValue = count;
        while (!Thread.currentThread().isInterrupted() && countValue > 0){
            try {
                Thread.sleep(500);
                Boolean result = (Boolean) redisTemplate.execute(refreshRedisScript, Collections.singletonList(lockKey),expireTime,refreshTime);
                if (result != null && result.equals(true)) {
                    countValue--;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
