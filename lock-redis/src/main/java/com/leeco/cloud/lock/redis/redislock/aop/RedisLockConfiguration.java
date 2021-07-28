package com.leeco.cloud.lock.redis.redislock.aop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis分布式锁配置类
 * @author liuqiang-068
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisLockConfiguration {

    @Bean
    public RedisLockPointCut redisLockPointCut(){
        return new RedisLockPointCut();
    }

    @Bean
    public RedisLockAdvice redisLockAdvice(){
        return new RedisLockAdvice();
    }

    @Bean
    public RedisLockAdvisor redisLockAdvisor(){
        return new RedisLockAdvisor(redisLockPointCut(),redisLockAdvice());
    }

}
