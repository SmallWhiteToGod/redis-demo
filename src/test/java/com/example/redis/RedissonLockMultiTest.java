package com.example.redis;

import com.example.redis.redisson.RedissonLockService;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.concurrent.*;

public class RedissonLockMultiTest extends BaseTestCase {

    @Autowired
    private RedissonLockService redissonLockService;
    @Autowired
    private RedissonClient redissonClient;

    ExecutorService threadPool = new ThreadPoolExecutor(20, 20, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @Test
    public void redissonMultiTest() throws Exception {
        int countDownNum = 150;
        CountDownLatch countDownLatch = new CountDownLatch(countDownNum);
        String key = "10000002";
        for (int i = 0; i < countDownNum; i++) {
            threadPool.execute(() -> {
                //随机判断是交易还是迁移
                if (Math.random() > 0.5) {
                    //模拟加迁移锁
                    Boolean lockFlag = false;
                    try {
                        if (lockFlag = redissonLockService.tryWriteLock(key)) {
                            //睡眠1s到2s 占用一会迁移锁
                            Thread.sleep(new Random().nextInt(1000) + 1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (lockFlag) {
                            redissonLockService.unWriteLock(key);
                        }
                    }
                } else {
                    //模拟加交易锁
                    Boolean lockFlag = false;
                    try {
                        if (lockFlag = redissonLockService.tryReadLock((key))) {
                            //睡眠1s之内 占用一会交易锁
                            Thread.sleep(new Random().nextInt(1000));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (lockFlag) {
                            redissonLockService.unReadLock(key);
                        }
                    }
                }
                countDownLatch.countDown();
            });

            if (i % 50 == 0) {
                //睡眠2.5s 每50个线程一组并发
                Thread.sleep(2500);
                System.out.println();
                System.out.println("===========2.5s later===========");
            }
        }

        countDownLatch.await();
        System.out.println("================");
    }
}
