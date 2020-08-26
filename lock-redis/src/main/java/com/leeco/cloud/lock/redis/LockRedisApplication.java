package com.leeco.cloud.lock.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 分布式锁 - redis实现
 * @author leeco
 */
@SpringBootApplication
public class LockRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockRedisApplication.class, args);
    }

}
