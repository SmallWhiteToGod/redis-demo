package com.example.redis;

import com.example.redis.redisson.RedissonLockService;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

public class RedissonLockTest extends BaseTestCase {

    @Autowired
    private RedissonLockService redissonLockService;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void redissonLockTest() throws Exception {

        String key = "100002";
        //单线程内可重入
        redissonLockService.tryReadLock(key);
        redissonLockService.tryReadLock(key);
        redissonLockService.tryWriteLock(key);
        redissonLockService.tryWriteLock(key);
        redissonLockService.tryReadLock(key);
        redissonLockService.tryReadLock(key);

        Thread.sleep(3000L);
        System.out.println("======3s later ======");
        System.out.println();

        redissonLockService.tryWriteLock(key);
        redissonLockService.tryWriteLock(key);
        redissonLockService.tryReadLock(key);
        redissonLockService.tryReadLock(key);
    }
}
