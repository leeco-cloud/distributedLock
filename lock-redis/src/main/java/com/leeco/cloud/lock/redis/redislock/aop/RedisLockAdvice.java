package com.leeco.cloud.lock.redis.redislock.aop;

import com.leeco.cloud.lock.redis.redislock.aop.annotation.RedisLock;
import com.leeco.cloud.lock.redis.redislock.lock.RedisLockLuaScript;
import com.leeco.cloud.lock.redis.redislock.lock.RedisLockRefreshDaemon;
import com.leeco.cloud.lock.redis.redislock.wheel.TimeWheel;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collections;


/**
 * redis分布式锁的通知
 * @author liuqiang-068
 */
public class RedisLockAdvice implements MethodInterceptor {

    private final Logger logger = LoggerFactory.getLogger(RedisLockAdvice.class);

    /**
     * 锁续期时间
     */
    private static final int refreshKeyTime = 3;

    /**
     * 续期次数
     */
    private static final int threshold = -1;

    /**
     * 默认锁过期时间
     */
    private static final int defaultExpirationTime = 5;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private TimeWheel timeWheel;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable{
        InetAddress ip4 = Inet4Address.getLocalHost();
        String hostIp = ip4.getHostAddress();
        Object result = null;
        Exception exception = null;
        RedisLock annotation = invocation.getMethod().getAnnotation(RedisLock.class);
        String uniqueKey = invocation.getThis().getClass() + "::" + invocation.getMethod().getName();
        for(;;) {
            RedisLockRefreshDaemon redisLockRefreshDaemon = tryLock(annotation,uniqueKey,hostIp);
            if (redisLockRefreshDaemon == null){
                continue;
            }
            boolean exceptionState = false;
            try{
                // 执行do
                result = invocation.proceed();
            }catch (Exception e){
                exceptionState = true;
                exception = e;
            }
            // 执行结束 主动释放锁锁
            unLock(annotation,uniqueKey,hostIp,redisLockRefreshDaemon);
            if (exceptionState){
                throw exception;
            }
            break;
        }
        return result;
    }

    /**
     * 抢夺锁
     */
    private RedisLockRefreshDaemon tryLock(RedisLock annotation,String uniqueKey, String hostIp) throws InterruptedException {
        // 可重入
        if (annotation.reentrant()){
            Long lockState = redisTemplate.execute(RedisLockLuaScript.getReentrantRedisScript(), Collections.singletonList(uniqueKey), hostIp, defaultExpirationTime);
            if (lockState == null || lockState.intValue() == -1){
                logger.info("===== 未抢占到锁:" + uniqueKey + ",阻塞等待100毫秒后重试 =====");
                Thread.sleep(100L);
                return null;
            }
            // 抢占到锁
            logger.info("===== 抢占锁成功 =====");
            RedisLockRefreshDaemon redisLockRefreshDaemon;
            if (lockState.intValue() == 1){
                // 启动守护线程
                redisLockRefreshDaemon = new RedisLockRefreshDaemon(redisTemplate, uniqueKey,threshold,refreshKeyTime);
                timeWheel.addJob(redisLockRefreshDaemon);
            }else{
                redisLockRefreshDaemon = timeWheel.getDaemon(uniqueKey);
            }
            return redisLockRefreshDaemon;
        }else{
            // 不可重入
            Boolean lockState = redisTemplate.execute(RedisLockLuaScript.getDefaultRedisScript(), Collections.singletonList(uniqueKey), hostIp, defaultExpirationTime);
            if (lockState == null || !lockState){
                logger.info("===== 未抢占到锁:" + uniqueKey + ",阻塞等待100毫秒后重试 =====");
                Thread.sleep(100L);
                return null;
            }
            // 抢占到锁
            logger.info("===== 抢占锁成功 =====");
            // 启动守护线程
            RedisLockRefreshDaemon redisLockRefreshDaemon = new RedisLockRefreshDaemon(redisTemplate, uniqueKey,threshold,refreshKeyTime);
            timeWheel.addJob(redisLockRefreshDaemon);
            return redisLockRefreshDaemon;
        }
    }

    /**
     * 释放锁
     */
    private void unLock(RedisLock annotation,String uniqueKey, String hostIp, RedisLockRefreshDaemon redisLockRefreshDaemon) {
        // 可重入
        if (annotation.reentrant()){
            Long unLockState = redisTemplate.execute(RedisLockLuaScript.getReentrantDeleteRedisScript(), Collections.singletonList(uniqueKey), hostIp);
            if (unLockState == null || unLockState.intValue() == -1){
                // 停止守护逻辑
                timeWheel.deleteDaemon(redisLockRefreshDaemon);
                logger.info("===== 释放锁失败 等待锁过期 =====");
            }else{
                if (unLockState.intValue() == 1){
                    // 停止守护逻辑
                    timeWheel.deleteDaemon(redisLockRefreshDaemon);
                }
                logger.info("===== 释放锁成功 =====");
            }
        }else{
            Boolean unLockState = redisTemplate.execute(RedisLockLuaScript.getDeleteRedisScript(), Collections.singletonList(uniqueKey), hostIp);
            // 停止守护逻辑
            timeWheel.deleteDaemon(redisLockRefreshDaemon);
            if (unLockState == null || !unLockState){
                logger.info("===== 释放锁失败 等待锁过期 =====");
            }else{
                logger.info("===== 释放锁成功 =====");
            }
        }
    }

}
