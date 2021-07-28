package com.leeco.cloud.lock.redis.redislock.aop;

import com.leeco.cloud.lock.redis.redislock.aop.annotation.RedisLock;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * redis分布式锁切点
 * @author liuqiang-068
 */
public class RedisLockPointCut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        RedisLock annotation = method.getAnnotation(RedisLock.class);
        return annotation != null;
    }

}
