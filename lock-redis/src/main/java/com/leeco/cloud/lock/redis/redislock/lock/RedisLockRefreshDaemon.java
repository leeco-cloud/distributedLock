package com.leeco.cloud.lock.redis.redislock.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * redis锁的续期线程 守护线程
 * @author liuqiang@ourdocker.cn
 */
public class RedisLockRefreshDaemon extends Thread {

    private final Logger logger = LoggerFactory.getLogger(RedisLockRefreshDaemon.class);

    private final String lockKey;

    private RedisTemplate<Object, Object> redisTemplate;

    public volatile boolean state;

    public RedisLockRefreshDaemon(RedisTemplate<Object, Object> redisTemplate,String lockKey){
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        setDaemon(true);
        state = true;
    }

    @Override
    public void run() {
        AtomicInteger expireSize = new AtomicInteger(5);
        for(;;){
            if (!state || expireSize.get() <= 0){
                logger.info("===== 退出守护线程 =====");
                break;
            }
            Boolean expire = redisTemplate.expire(lockKey, 5, TimeUnit.SECONDS);
            if (expire == null || !expire){
                throw new RuntimeException();
            }
            try {
                expireSize.decrementAndGet();
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                logger.info("===== 销毁守护线程 =====");
                break;
            }
        }
    }

}
