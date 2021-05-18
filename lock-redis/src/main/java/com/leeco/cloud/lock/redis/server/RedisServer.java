package com.leeco.cloud.lock.redis.server;

import com.leeco.cloud.lock.redis.redislock.aop.annotation.RedisLock;
import org.springframework.stereotype.Component;

/**
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/26 21:33
 */
@Component
public class RedisServer {

    /**
     * 将redis锁利用AOP封装成了注解型组件
     */
    @RedisLock(rollBackFor = RuntimeException.class)
    public void demo(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
