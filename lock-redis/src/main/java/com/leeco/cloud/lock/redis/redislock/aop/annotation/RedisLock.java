package com.leeco.cloud.lock.redis.redislock.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * redis分布式锁
 * @author liuqiang-068
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisLock {

    /**
     * 是否可重入 默认不可重入
     */
    boolean reentrant() default false;

}
