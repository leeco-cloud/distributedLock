package com.leeco.cloud.mysql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 分布式锁 - mysql实现
 * @author leeco
 */
@SpringBootApplication
public class LockMysqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockMysqlApplication.class, args);
    }

}
