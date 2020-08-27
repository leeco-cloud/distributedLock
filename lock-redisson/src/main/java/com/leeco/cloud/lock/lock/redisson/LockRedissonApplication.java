package com.leeco.cloud.lock.lock.redisson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LockRedissonApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockRedissonApplication.class, args);
    }

}
