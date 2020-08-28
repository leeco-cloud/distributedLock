package com.leeco.cloud.lock.lock.zookeeper.lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;

/**
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/28 21:53
 */
@Configuration
public class ZookeeperConfig {

    @Value("${zookeeper.address}")
    private  String connectString;

    @Bean(name = "zkClient")
    public ZooKeeper zkClient(){
        ZooKeeper zooKeeper=null;
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(connectString, 10000, event -> {
                if(Watcher.Event.KeeperState.SyncConnected==event.getState()){
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            System.out.println("zk connect success...");
            // 创建锁的持久化节点
            zooKeeper.create("/lock", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }catch (Exception e){
            e.printStackTrace();
        }
        return zooKeeper;
    }

}
