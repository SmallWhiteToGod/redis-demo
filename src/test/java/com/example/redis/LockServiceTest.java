package com.example.redis;

import com.example.redis.service.impl.BusinessLockService;
import com.example.redis.service.impl.TransferLockService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

public class LockServiceTest extends BaseTestCase {
    @Autowired
    private TransferLockService transferLockService;
    @Autowired
    private BusinessLockService businessLockService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void lockTest() throws Exception {

//        Thread.sleep(30000L);
//        System.out.println("==== 30s later ====");

        String key = "myLock";
        String transferKey = transferLockService.TRANSFER_LOCK_PRIFIX + ":" + key;
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        //加交易锁
        businessLockService.tryLock(key, uuid1);
        businessLockService.unLock(key, uuid1);
        //加迁移锁
        if (transferLockService.tryLock(key, uuid2)) {
            String value = stringRedisTemplate.opsForValue().get(transferKey);
            System.out.println("value: " + value + " ttl: " + stringRedisTemplate.getExpire(transferKey));

            transferLockService.unLock(key, uuid2);
            value = stringRedisTemplate.opsForValue().get(transferKey);
            System.out.println("value: " + value + " ttl: " + stringRedisTemplate.getExpire(transferKey));
        }
    }
}
