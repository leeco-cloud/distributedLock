package com.leeco.cloud.lock.redis.redislock.wheel;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolWheel {

    public static ThreadPoolTaskExecutor initThreadPool(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(5);
        //配置最大线程数
        executor.setMaxPoolSize(10);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("TimeWheel-Second-");
        // CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(50);
        //执行初始化
        executor.initialize();
        return executor;
    }

}
