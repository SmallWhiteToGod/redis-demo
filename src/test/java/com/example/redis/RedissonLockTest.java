package com.example.redis;

import com.example.redis.redisson.LockTestDemo;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class RedissonLockTest extends BaseTestCase {

    @Autowired
    private LockTestDemo lockTestDemo;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void redissonLockTest() throws Exception {

        String custUid = "1000002";
        String[] custUids = new String[]{"1000001","1000002","1000003"};


        System.out.println("====================");
        lockTestDemo.business(custUid);
        System.out.println("====================");
        lockTestDemo.transfer(custUid);
        System.out.println("====================");
        lockTestDemo.companyBusiness(custUids);
        System.out.println("====================");
        lockTestDemo.companyTransfer(custUids);

        redissonClient.shutdown();
        System.out.println("Complete!");
    }
}
