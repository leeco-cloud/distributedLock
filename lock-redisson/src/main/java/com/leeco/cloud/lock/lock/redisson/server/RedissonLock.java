package com.leeco.cloud.lock.lock.redisson.server;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/27 19:42
 */
@Component
public class RedissonLock implements ApplicationRunner {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String lockKey = "lock";
        RLock lock = redissonClient.getLock(lockKey);

        lock.lock(10, TimeUnit.SECONDS);
        try{
            System.out.println("执行业务代码...");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
