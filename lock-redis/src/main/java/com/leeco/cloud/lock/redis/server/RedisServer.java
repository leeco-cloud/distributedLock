package com.leeco.cloud.lock.redis.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/26 21:33
 */
@Component
@SuppressWarnings("unchecked")
public class RedisServer {

    private final Logger logger = LoggerFactory.getLogger(RedisServer.class);

    private final RedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> lockRedisScript;

    private final DefaultRedisScript<Boolean> unLockRedisScript;

    private final DefaultRedisScript<Boolean> refreshRedisScript;

    public RedisServer(RedisTemplate redisTemplate, DefaultRedisScript<Long> lockRedisScript, DefaultRedisScript<Boolean> unLockRedisScript, DefaultRedisScript<Boolean> refreshRedisScript) {
        this.redisTemplate = redisTemplate;
        this.lockRedisScript = lockRedisScript;
        this.unLockRedisScript = unLockRedisScript;
        this.refreshRedisScript = refreshRedisScript;
    }

    /**
     * 执行上锁的lua脚本
     */
    private Long lockLua(String key, String value){
        try{
            List<String> keys = Collections.singletonList(key);
            return (Long) redisTemplate.execute(lockRedisScript, keys, value, 10);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return -1L;
        }
    }

    /**
     * 执行释放锁的lua脚本
     */
    private void unLockLua(String key, String value){
        try{
            List<String> keys = Collections.singletonList(key);
            redisTemplate.execute(unLockRedisScript, keys, value);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }

    /**
     * 版本 1
     * 1. 先查看是否可用锁
     * 2. 若可用 则占用锁
     */
    public void version1(){
        String key = "lock";
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null){
            value = Thread.currentThread().getName();
            redisTemplate.opsForValue().set(key,value);
            // 业务操作
            try{
                System.out.println("执行业务操作...");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisTemplate.delete(key);
            }
        }
    }

    /**
     * 版本 2
     * 1. 先查看是否可用锁
     * 2. 若可用 则占用锁
     * 3. 给锁设置过期时间
     */
    public void version2(){
        String key = "lock";
        Long time = redisTemplate.getExpire(key);
        if (time == null || time < 0){
            redisTemplate.opsForValue().set(key,Thread.currentThread().getName());
            redisTemplate.expire(key,10, TimeUnit.SECONDS);
            // 业务操作
            try{
                System.out.println("执行业务操作...");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisTemplate.delete(key);
            }
        }
    }

    /**
     * 版本 3
     * 1. 先查看是否可用锁
     * 2. 若可用 则占用锁
     * 3. 给锁设置过期时间
     * 这三个步骤利用 lua 脚本保证"原子性"(相比于不使用事务)
     * 4. 利用 lua 脚本释放锁
     */
    public void version3(){

        String key = "lock";
        String value = Thread.currentThread().getName();

        Long result = lockLua(key, value);
        if (result == 0){
            // 业务操作
            try{
                System.out.println("执行业务操作...");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                unLockLua(key, value);
            }
        }
    }

    /**
     * 版本 4
     * 1. 先查看是否可用锁
     * 2. 若可用 则占用锁
     * 3. 给锁设置过期时间
     * 这三个步骤利用 lua 脚本保证"原子性"(相比于不使用事务)
     * 4. 利用 lua 脚本释放锁
     * 5. 启动一个逻辑上的守护线程, 给锁续期
     */
    public void version4(){

        String key = "lock";
        String value = Thread.currentThread().getName();

        Long result = lockLua(key, value);
        if (result == 0){

            /*
            起一个线程 给锁续期
             */
            Thread refreshLock = new RefreshLock(key, 10L, 3, 3L, redisTemplate, refreshRedisScript);
            try{
                refreshLock.start();
                // 业务操作
                System.out.println("执行业务操作...");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                unLockLua(key, value);
                refreshLock.interrupt();
            }
        }
    }

    /**
     * 版本 5
     * 1. 先查看是否可用锁
     * 2. 若可用 则占用锁
     * 3. 给锁设置过期时间
     * 这三个步骤利用 lua 脚本保证"原子性"(相比于不使用事务)
     * 4. 利用 lua 脚本释放锁
     * 5. 启动一个逻辑上的守护线程, 给锁续期
     * 6. 如果没有获取到锁 则自旋等待
     */
    public void version5() throws InterruptedException {

        String key = "lock";
        String value = Thread.currentThread().getName();

        for(;;){
            // 自旋 去获取锁
            Long result = lockLua(key, value);
            if (result == 0){
                break;
            }
            Thread.sleep(100);
        }

        // 守护线程
        Thread refreshLock = new RefreshLock(key, 10L, 3, 3L, redisTemplate, refreshRedisScript);
        try{
            refreshLock.start();
            // 业务操作
            System.out.println("执行业务操作...");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            unLockLua(key, value);
            refreshLock.interrupt();
        }
    }

}
