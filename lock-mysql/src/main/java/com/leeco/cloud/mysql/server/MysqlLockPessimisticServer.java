package com.leeco.cloud.mysql.server;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * mysql 悲观锁
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/27 22:45
 */
@Component
public class MysqlLockPessimisticServer implements ApplicationRunner{

    /** 事务管理器顶层接口 PlatformTransactionManager */
    private final PlatformTransactionManager platformTransactionManager;

    /** jdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    /** 默认的事务定义 */
    private final TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    public MysqlLockPessimisticServer(PlatformTransactionManager platformTransactionManager, JdbcTemplate jdbcTemplate) {
        this.platformTransactionManager = platformTransactionManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        try{
            Long threadId = null;
            String lockKey = "lockKey";
            int result = 0;
            try{
                // 尝试插入key获取锁
                threadId = Thread.currentThread().getId();
                jdbcTemplate.execute("insert into lock(lock_key,thread_id) values (" + lockKey +","+ threadId +");");
                // 抢占锁
                lock(result,threadId);
            }catch (Exception e){
                // 如果抛出异常 说明该key已经存在 则直接去抢占锁
                lock(result,threadId);
            }
            // 业务代码
            System.out.println("业务代码");

            // 释放锁 并不是直接释放  而是先减少重入次数
            jdbcTemplate.execute("update lock set entry_count = entry_count - 1 where thread_id is not null and thread_id = " + threadId);
            // 若不存在重入次数 则删除锁
            jdbcTemplate.execute("update lock set thread_id = null where lock_key = "+ lockKey +" and entry_count <= 0;");
            // 提交事务
            platformTransactionManager.commit(transaction);
        }catch (Exception e){
            e.printStackTrace();
            // 回滚事务
            platformTransactionManager.rollback(transaction);
        }
    }

    /**
     * 自旋获取锁
     */
    private void lock(int result,Long threadId){
        while(result == 0){
            result = jdbcTemplate.update("update lock set entry_count = entry_count + 1 where thread_id is not null and thread_id = " + threadId);
        }
    }

}