package com.leeco.cloud.lock.redis.redislock.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * redis锁的续期线程 守护线程
 * @author liuqiang-068
 */
public class RedisLockRefreshDaemon extends Thread {

    private final Logger logger = LoggerFactory.getLogger(RedisLockRefreshDaemon.class);

    private final String lockKey;

    RedisTemplate<Object, Object> redisTemplate;

    /**
     * 执行次数 默认-1 为一直执行
     */
    public volatile int threshold;

    /**
     * 执行时间
     */
    public volatile int refreshKeyTime;

    /**
     * 是否可以正常续期
     */
    public volatile boolean state;

    public RedisLockRefreshDaemon(RedisTemplate<Object, Object> redisTemplate,String lockKey, int threshold,int refreshKeyTime){
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.threshold = threshold;
        this.refreshKeyTime = refreshKeyTime;
        state = true;
    }

    @Override
    public void run() {
        Boolean expire = redisTemplate.expire(lockKey, 5, TimeUnit.SECONDS);
        if (expire == null || !expire){
            logger.info("lockKey:" + lockKey + " 续期:" + "失败");
            state = false;
            return;
        }
        logger.info("lockKey:" + lockKey + " 续期:" + "成功");
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getRefreshKeyTime() {
        return refreshKeyTime;
    }

    public void setRefreshKeyTime(int refreshKeyTime) {
        this.refreshKeyTime = refreshKeyTime;
    }

    public String getLockKey() {
        return lockKey;
    }
}
