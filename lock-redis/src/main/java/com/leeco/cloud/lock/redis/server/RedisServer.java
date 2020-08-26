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

    private final DefaultRedisScript<Long> defaultRedisScript;

    public RedisServer(RedisTemplate redisTemplate, DefaultRedisScript<Long> defaultRedisScript) {
        this.redisTemplate = redisTemplate;
        this.defaultRedisScript = defaultRedisScript;
    }

    private Long lockLua(String key, String value, Integer expire){
        try{
            List<String> keys = Collections.singletonList(key);
            Long result = (Long) redisTemplate.execute(defaultRedisScript, keys, value, expire);
            return result;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return -1L;
        }
    }

    /**
     * version 1
     */
    public void version1(){
        String key = "lock";
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null){
            value = "version1";
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
     * version 2
     */
    public void version2(){
        String key = "lock";
        Long time = redisTemplate.getExpire(key);
        if (time == null || time < 0){
            String value = "version2";
            redisTemplate.opsForValue().set(key,value);
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
     * version 3
     */
    public void version3(){

        String key = "lock";
        String value = "version3";

        Long result = lockLua(key, value, 100);
        if (result == 0){
            // 业务操作
            try{
                System.out.println("执行业务操作...");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
//                unLockLua(key, value);
            }
        }
    }

}
