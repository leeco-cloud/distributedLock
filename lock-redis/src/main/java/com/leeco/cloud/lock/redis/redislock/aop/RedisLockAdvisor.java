package com.leeco.cloud.lock.redis.redislock.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * redis分布式锁的切面
 * @author liuqiang-068
 */
public class RedisLockAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private final RedisLockPointCut redisLockPointCut;

    private final RedisLockAdvice redisLockAdvice;

    public RedisLockAdvisor(RedisLockPointCut redisLockPointCut, RedisLockAdvice redisLockAdvice) {
        this.redisLockPointCut = redisLockPointCut;
        this.redisLockAdvice = redisLockAdvice;
    }

    /**
     * getPointcut
     * @return Pointcut
     */
    @Override
    public Pointcut getPointcut() {
        return redisLockPointCut;
    }

    @Override
    public Advice getAdvice() {
        return redisLockAdvice;
    }

}
