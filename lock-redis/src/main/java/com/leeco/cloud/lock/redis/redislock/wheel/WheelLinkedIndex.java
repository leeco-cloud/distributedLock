package com.leeco.cloud.lock.redis.redislock.wheel;


import com.leeco.cloud.lock.redis.redislock.lock.RedisLockRefreshDaemon;

import java.util.concurrent.LinkedBlockingQueue;

public class WheelLinkedIndex {

    public WheelLinkedIndex(int index) {
        this.index = index;
    }

    public WheelLinkedIndex(int index, WheelLinkedIndex pre, LinkedBlockingQueue<RedisLockRefreshDaemon> queue) {
        this.index = index;
        this.pre = pre;
        this.queue = queue;
    }

    private int index;

    private WheelLinkedIndex pre;

    private WheelLinkedIndex next;

    private LinkedBlockingQueue<RedisLockRefreshDaemon> queue;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public WheelLinkedIndex getPre() {
        return pre;
    }

    public void setPre(WheelLinkedIndex pre) {
        this.pre = pre;
    }

    public WheelLinkedIndex getNext() {
        return next;
    }

    public void setNext(WheelLinkedIndex next) {
        this.next = next;
    }

    public LinkedBlockingQueue<RedisLockRefreshDaemon> getQueue() {
        return queue;
    }

    public void setQueue(LinkedBlockingQueue<RedisLockRefreshDaemon> queue) {
        this.queue = queue;
    }
}
