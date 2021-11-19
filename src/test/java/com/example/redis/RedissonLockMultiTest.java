package com.example.redis;

import com.example.redis.redisson.LockTestDemo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.concurrent.*;

public class RedissonLockMultiTest extends BaseTestCase {

    @Autowired
    private LockTestDemo lockTestDemo;

    ExecutorService threadPool = new ThreadPoolExecutor(10, 10, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @Test
    public void redissonMultiTest() throws Exception {
        int countDownNum = 100;
        CountDownLatch countDownLatch = new CountDownLatch(countDownNum);
        String custUid = "1000002";
        String[] custUids = new String[]{"1000001","1000002","1000003"};

        for (int i = 0; i < countDownNum; i++) {
            threadPool.execute(() -> {
                //随机判断是交易还是迁移
                double randomNum = Math.random();
                String type = "";
                if (randomNum > 0.6) {
                   type = "business";
                } else if (randomNum > 0.2){
                    type = "transfer";
                } else if (randomNum > 0.1) {
                    type = "companyBusiness";
                } else {
                    type = "companyTransfer";
                }

                switch (type) {
                    case "business":
                        lockTestDemo.business(custUid);
                        break;
                    case "transfer":
                        lockTestDemo.transfer(custUid);
                        break;
                    case "companyBusiness":
                        lockTestDemo.companyBusiness(custUids);
                        break;
                    case "companyTransfer":
                        lockTestDemo.companyTransfer(custUids);
                        break;
                    default:break;
                }
                countDownLatch.countDown();
            });

            if (i % 10 == 0) {
                //睡眠2s 每10个请求一组并发
                Thread.sleep(2000);
                System.out.println();
                System.out.println("===========2.5s later===========");
            }
        }

        countDownLatch.await();
        System.out.println("================");
    }
}
