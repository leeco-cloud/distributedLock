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
 * @author liuqiang@ourdocker.cn
 * @version 0.0.1
 * @date 2020/8/27 22:45
 */
@Component
public class MysqlLockServer implements ApplicationRunner{

    /** 事务管理器顶层接口 PlatformTransactionManager */
    private final PlatformTransactionManager platformTransactionManager;

    /** jdbcTemplate */
    private final JdbcTemplate jdbcTemplate;

    /** 默认的事务定义 */
    private final TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    public MysqlLockServer(PlatformTransactionManager platformTransactionManager, JdbcTemplate jdbcTemplate) {
        this.platformTransactionManager = platformTransactionManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        try{
            try{
                // 尝试插入key获取锁
                jdbcTemplate.execute("insert into `lock`(`lock_key`) values (1);");
                // 抢占锁
                jdbcTemplate.execute("select 1 from `lock` where lock_key = 1 for update;");
            }catch (Exception e){
                // 如果抛出异常 说明该key已经存在 则直接去抢占锁
                jdbcTemplate.execute("select 1 from `lock` where lock_key = 1 for update;");
            }
            // 业务代码
            System.out.println("业务代码");

            // 删除锁
            jdbcTemplate.execute("delete from `lock` where lock_key = 1;");
            // 提交事务
            platformTransactionManager.commit(transaction);
        }catch (Exception e){
            e.printStackTrace();
            // 回滚事务
            platformTransactionManager.rollback(transaction);
        }
    }

}