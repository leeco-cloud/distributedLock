package com.leeco.cloud.lock.redis.redislock.aop;

import com.leeco.cloud.lock.redis.redislock.aop.annotation.RedisLock;
import com.leeco.cloud.lock.redis.redislock.lock.RedisLockLuaScript;
import com.leeco.cloud.lock.redis.redislock.lock.RedisLockRefreshDaemon;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * redis分布式锁的通知
 * @author liuqiang@ourdocker.cn
 */
public class RedisLockAdvice implements MethodInterceptor {

    private final Logger logger = LoggerFactory.getLogger(RedisLockAdvice.class);

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(6, 20, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(12), new DefaultThreadFactory("task-lock-daemon"), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable{
        long threadId = Thread.currentThread().getId();
        Object result = null;
        RedisLock annotation = invocation.getMethod().getAnnotation(RedisLock.class);
        String uniqueKey = invocation.getThis().getClass() + "::" + invocation.getMethod().getName();
        for(;;) {
            // 抢占锁
            Boolean lock = redisTemplate.execute(RedisLockLuaScript.getDefaultRedisScript(), Collections.singletonList(uniqueKey), threadId, 5);
            if (lock != null && lock) {
                // 抢占到锁
                logger.info("===== 抢占锁成功 =====");
                // 启动守护线程
                RedisLockRefreshDaemon redisLockRefreshDaemon = new RedisLockRefreshDaemon(redisTemplate, uniqueKey);
                EXECUTOR.execute(redisLockRefreshDaemon);
                boolean exception = false;
                try{
                    // 执行do
                    result = invocation.proceed();
                }catch (Exception e){
                    exception = true;
                }
                // 停止守护线程
                redisLockRefreshDaemon.state = false;
                // 执行结束 主动释放锁锁
                Boolean deleted = redisTemplate.execute(RedisLockLuaScript.getDeleteRedisScript(), Collections.singletonList(uniqueKey), threadId);
                if (deleted != null && deleted) {
                    logger.info("===== 释放锁成功 =====");
                }else{
                    logger.info("===== 释放锁失败 等待锁过期 =====");
                }
                if (exception){
                    throw annotation.rollBackFor().newInstance();
                }
                break;
            }
            logger.info("===== 未抢占到锁 =====");
        }
        return result;

    }

}
