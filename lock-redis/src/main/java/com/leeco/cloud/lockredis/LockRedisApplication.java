package com.leeco.cloud.lockredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LockRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockRedisApplication.class, args);
    }

}
