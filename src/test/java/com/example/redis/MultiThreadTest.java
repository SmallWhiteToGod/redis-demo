package com.example.redis;

import com.example.redis.service.impl.BusinessLockService;
import com.example.redis.service.impl.TransferLockService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadTest extends BaseTestCase {

    ExecutorService threadPool = new ThreadPoolExecutor(20, 20, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @Autowired
    private TransferLockService transferLockService;
    @Autowired
    private BusinessLockService businessLockService;

    @Test
    public void multiThreadTest() throws InterruptedException {
        int countDownNum = 1500;
        CountDownLatch countDownLatch = new CountDownLatch(countDownNum);
        String key = "10000001";

        //每50个并发去抢占锁。抢到迁移锁,则其他线程不会加锁成功；抢到交易锁,其他线程可以加交易锁,无法加迁移锁
        for (int i = 0; i < countDownNum; i++) {
            threadPool.execute(() -> {
                String uuid =  UUID.randomUUID().toString();
                //随机判断是交易还是迁移
                if (Math.random() > 0.5) {
                    if (transferLockService.tryLock(key, uuid)) {
                        try {
                            //睡眠1s到2s 占用一会迁移锁
                            Thread.sleep(new Random().nextInt(1000) + 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //手动解迁移锁
                        transferLockService.unLock(key, uuid);
                    }
                } else {
                    if (businessLockService.tryLock(key, uuid)) {
                        //睡眠1s之内 占用一会交易锁
                        try {
                            Thread.sleep(new Random().nextInt(1000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                countDownLatch.countDown();
            });

            if (i % 50 == 0) {
                //睡眠2.5s
                Thread.sleep(2500);
                System.out.println();
                System.out.println("===========2.5s later===========");
            }
        }

        countDownLatch.await();
        System.out.println("================");
    }
}
