package com.example.redis;

import com.example.redis.redisson.impl.BusinessLockService;
import com.example.redis.redisson.impl.TransferLockService;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RedissonLockTest extends AbstractJUnit4SpringContextTests {
    @Autowired
    private BusinessLockService businessLockService;
    @Autowired
    private TransferLockService transferLockService;
    @Autowired
    private RedissonClient redissonClient;

    ExecutorService threadPool = new ThreadPoolExecutor(20, 20, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @Test
    public void redissonLockTest() throws Exception {
        Thread.sleep(3000L);

        String key1 = "100002";
        String key2 = "100003";
        String key3 = "100004";

        List<String> keys = new ArrayList<>();
        keys.add(key1);
        keys.add(key2);
        keys.add(key3);
        businessLockService.tryMultiLock(keys);
        transferLockService.tryMultiLock(keys);

        Thread.sleep(3000L);
        System.out.println("======3s later ======");


        threadPool.execute(()->{
            try {
                businessLockService.tryLock(key2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        threadPool.execute(()->{
            try {
                transferLockService.tryLock(key3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
