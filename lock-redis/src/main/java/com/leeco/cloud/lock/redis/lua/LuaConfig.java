package com.leeco.cloud.lock.redis.lua;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/26 20:33
 */
@Configuration
public class LuaConfig {

    @Bean
    public DefaultRedisScript<Long> lockRedisScript(){
        ClassPathResource resource = new ClassPathResource("lock.lua");
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(resource));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Boolean> unLockRedisScript(){
        ClassPathResource resource = new ClassPathResource("unLock.lua");
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(resource));
        script.setResultType(Boolean.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Boolean> refreshRedisScript(){
        ClassPathResource resource = new ClassPathResource("refreshLock.lua");
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(resource));
        script.setResultType(Boolean.class);
        return script;
    }

    /**
     * 设置 redisTemplate 的序列化设置
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 1.创建 redisTemplate 模版
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        // 2.关联 redisConnectionFactory
        template.setConnectionFactory(redisConnectionFactory);
        // 3.创建 序列化类
        GenericToStringSerializer genericToStringSerializer = new GenericToStringSerializer(Object.class);
        // 6.序列化类，对象映射设置
        // 7.设置 value 的转化格式和 key 的转化格式
        template.setValueSerializer(genericToStringSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

}
