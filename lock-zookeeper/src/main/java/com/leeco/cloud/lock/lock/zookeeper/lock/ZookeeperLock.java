package com.leeco.cloud.lock.lock.zookeeper.lock;

import org.apache.zookeeper.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

/**
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/28 21:52
 */
@Component
public class ZookeeperLock implements ApplicationRunner {

    // 前一个节点的临时节点
    private String watchLock;
    // 当前拥有的节点
    private String currentLock;
    // 阻塞等待锁的线程
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    // 临时节点的前缀名 例：lock-00000001
    private static final String LOCK_PRE ="lock-";

    private final ZooKeeper zkClient;

    public ZookeeperLock(ZooKeeper zkClient) {
        this.zkClient = zkClient;
    }

    /**
     * 抢占锁
     */
    private boolean tryLock() throws KeeperException, InterruptedException {
        if (currentLock == null){
            synchronized (this){
                if (currentLock == null){
                    // 创建临时节点
                    currentLock = zkClient.create("/lock/" + LOCK_PRE, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                    System.out.println(currentLock);
                }
            }
        }
        return isFirstNode();
    }

    /**
     * 判断是否是第一个节点
     */
    private boolean isFirstNode() throws KeeperException, InterruptedException {
        List<String> childrenList = zkClient.getChildren("/lock", false);
        TreeSet<String> sortedSet = new TreeSet<>();
        for (String children : childrenList) {
            sortedSet.add("/lock/" + children);
        }
        //获得当前节点中的最小的子节点;
        String firstNode = sortedSet.first();
        SortedSet<String> lessThenCurrentNode = sortedSet.headSet(currentLock);
        //通过当前节点与最小节点进行比较 , 如果相等则获取所成功;
        if (currentLock.equals(firstNode)) {
            return true;
        }
        // 获得比当前节点更小的最后一个节点，进行监听
        if (!lessThenCurrentNode.isEmpty()) {
            watchLock = lessThenCurrentNode.last();
            waitLock();
            countDownLatch.await();
            isFirstNode();
        }
        return false;
    }

    /**
     * 阻塞等待获取到锁
     */
    private void waitLock() throws KeeperException, InterruptedException {
        zkClient.exists(watchLock, event -> {
            if (event.getType() == Watcher.Event.EventType.NodeDeleted){
                countDownLatch.countDown();
            }
        });
    }

    /**
     * 释放锁
     */
    private void unLock() throws KeeperException, InterruptedException {
        // 设置version为-1 强制删除
        zkClient.delete(currentLock,-1);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try{
            if (tryLock()){
                // 执行业务代码
                System.out.println(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            unLock();
        }
    }

}
