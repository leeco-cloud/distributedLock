# distributedLock
分布式锁的实现 基于redis/redisson/zookeeper/mysql分别实现

redis锁架构图：
![](https://img-blog.csdnimg.cn/10c2a04973024ad1bf3cbd1e10ee8bcf.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWNodW50YW5nMjcyOQ==,size_16,color_FFFFFF,t_70#pic_center)


## 使用方式：
    
```java
    /*
    * reentrant:是否设置为可重入 默认为false
    */
    @RedisLock(reentrant = false)
    public void demo(){
        try {
            // ...业务逻辑
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```
