package com.leeco.cloud.lock.redis.redislock.wheel;

import com.leeco.cloud.lock.redis.redislock.lock.RedisLockRefreshDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TimeWheel implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TimeWheel.class);

    /** 秒轮 */
    private static final WheelLinkedIndex head = new WheelLinkedIndex(1,null,new LinkedBlockingQueue<>());

    private static final Map<Integer,WheelLinkedIndex> wheelMap = new ConcurrentHashMap<>(1 << 6);

    private static final Map<String, RedisLockRefreshDaemon> redisLockRefreshDaemonMap = new ConcurrentHashMap<>(1 << 6);

    /** 当前执行的时间索引 */
    private volatile int currentIndex = 0;

    private static final ThreadPoolTaskExecutor executor;

    static{
        // 初始化秒轮
        WheelLinkedIndex pre = head;
        wheelMap.put(0,pre);
        for (int i = 1;i < 60; i++){
            WheelLinkedIndex next = new WheelLinkedIndex(i,pre,new LinkedBlockingQueue<>());
            pre.setNext(next);
            pre = next;
            wheelMap.put(i,next);
        }
        executor = ThreadPoolWheel.initThreadPool();
    }

    /** 启动 */
    public void start() throws InterruptedException {
        WheelLinkedIndex currentWheel = head;
        while(currentWheel != null){
            currentIndex = currentWheel.getIndex();
            LinkedBlockingQueue<RedisLockRefreshDaemon> queue = currentWheel.getQueue();
            if (queue == null){
                currentWheel = currentWheel.getNext();
                Thread.sleep(1000);
                continue;
            }
            queue.forEach(redisLockRefreshDaemon -> {
                queue.remove(redisLockRefreshDaemon);
                if (!redisLockRefreshDaemonMap.containsValue(redisLockRefreshDaemon)){
                    return;
                }
                if (!redisLockRefreshDaemon.state){
                    // 续期失败且不存在key 则退出
                    deleteDaemon(redisLockRefreshDaemon);
                    return;
                }
                executor.execute(redisLockRefreshDaemon);
                if (redisLockRefreshDaemon.getThreshold() == 1){
                    // 移除该任务
                    deleteDaemon(redisLockRefreshDaemon);
                    return;
                }
                if (redisLockRefreshDaemon.getThreshold() != -1){
                    redisLockRefreshDaemon.setThreshold(redisLockRefreshDaemon.getThreshold() - 1);
                }
                deleteDaemon(redisLockRefreshDaemon);
                addJob(redisLockRefreshDaemon);
            });
            currentWheel = currentWheel.getNext() == null ? head : currentWheel.getNext();
            Thread.sleep(1000);
        }
    }

    /**
     * 添加任务
     * @param redisLockRefreshDaemon RedisLockRefreshDaemon
     */
    public void addJob(RedisLockRefreshDaemon redisLockRefreshDaemon){
        if (redisLockRefreshDaemon.refreshKeyTime >= 1){
            int relic = (redisLockRefreshDaemon.refreshKeyTime + currentIndex) % 60;
            WheelLinkedIndex wheelLinkedIndex = wheelMap.get(relic);
            LinkedBlockingQueue<RedisLockRefreshDaemon> queue = wheelLinkedIndex.getQueue();
            // 创建job静态代理
            queue.add(redisLockRefreshDaemon);
            redisLockRefreshDaemonMap.putIfAbsent(redisLockRefreshDaemon.getLockKey(),redisLockRefreshDaemon);
        }else{
            logger.info(">>>>>>>> 续期时间发生异常 请输入大于等于1的数字 <<<<<<<<<");
        }
    }

    /**
     * 删除任务
     * @param redisLockRefreshDaemon RedisLockRefreshDaemon
     */
    public void deleteDaemon(RedisLockRefreshDaemon redisLockRefreshDaemon){
        redisLockRefreshDaemonMap.remove(redisLockRefreshDaemon.getLockKey());
    }

    /**
     * 获取指定的守护进程
     * @param uniqueKey uniqueKey
     * @return RedisLockRefreshDaemon
     */
    public RedisLockRefreshDaemon getDaemon(String uniqueKey){
        return redisLockRefreshDaemonMap.get(uniqueKey);
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            start();
        } catch (InterruptedException e) {
            logger.error(">>>>>>>> 时间轮启动失败 请重启 <<<<<<<<<");
            System.exit(-1);
        }
    }

}
