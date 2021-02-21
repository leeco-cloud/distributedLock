package com.leeco.cloud.lock.redis.controller;

import com.leeco.cloud.lock.redis.server.RedisServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/26 23:35
 */
@RestController
public class LockController {

    private final RedisServer redisServer;

    public LockController(RedisServer redisServer) {
        this.redisServer = redisServer;
    }

    @GetMapping("/lock")
    public void lock() throws InterruptedException {
        redisServer.version5();
    }

}
