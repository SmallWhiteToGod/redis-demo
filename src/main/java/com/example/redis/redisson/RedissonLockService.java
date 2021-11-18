package com.example.redis.redisson;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class RedissonLockService {
    //迁移锁前缀
    public final static String TRANSFER_LOCK_PRIFIX = "transfer:lock";
    //加锁等待时间 单位ms
    public final static Long LOCK_WAIT_TIME = 50L;
    //迁移锁过期时间 单位ms
    public final static Long TRANSFER_LOCK_EXPIRE_TIME = 60L * 1000L;
    //交易锁过期时间 单位ms
    public final static Long BUSINESS_LOCK_EXPIRE_TIME = 60L * 1000L;

    @Autowired
    private RedissonClient redissonClient;

    public Boolean tryReadLock(String key) throws InterruptedException {
        RReadWriteLock lock = redissonClient.getReadWriteLock(this.extendBusinessKey(key));
        Boolean res = lock.readLock().tryLock(LOCK_WAIT_TIME, BUSINESS_LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        System.out.printf("[%s]——交易锁申请%s, key: [%s]%n",
                Thread.currentThread().getName(), res ? "成功" : "失败", key);
        return res;
    }

    public void unReadLock(String key) {
        RReadWriteLock lock = redissonClient.getReadWriteLock(this.extendBusinessKey(key));
        lock.readLock().unlock();
        System.out.printf("[%s]——交易锁解锁, key: [%s]%n", Thread.currentThread().getName(), key);    }

    public Boolean tryWriteLock(String key) throws InterruptedException {
        RReadWriteLock lock = redissonClient.getReadWriteLock(this.extendTransferKey(key));
        Boolean res = lock.writeLock().tryLock(LOCK_WAIT_TIME, TRANSFER_LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        
        System.out.printf("[%s]——迁移锁申请%s, key: [%s]%n", 
                Thread.currentThread().getName(), res ? "成功" : "失败", key);
        return res;
    }

    public void unWriteLock(String key) {
        RReadWriteLock lock = redissonClient.getReadWriteLock(this.extendTransferKey(key));
        lock.writeLock().unlock();
        System.out.printf("[%s]——迁移锁解锁, key: [%s]%n", Thread.currentThread().getName(), key);
    }

    //联锁 todo
    public Boolean tryMultiLock(List<String> keys) throws InterruptedException {
        RLock[] locks = new RLock[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            locks[i] = redissonClient.getLock(this.extendTransferKey(keys.get(i)));
        }

        RedissonMultiLock multiLock = new RedissonMultiLock(locks);
        Boolean res = multiLock.tryLock(LOCK_WAIT_TIME, TRANSFER_LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        System.out.printf("[%s]——联锁申请%s, keys: %s%n",
                Thread.currentThread().getName(), res ? "成功" : "失败", Arrays.toString(keys.toArray()));
        return res;
    }

    /**
     * 装饰迁移锁的key
     * @param key
     * @return
     */
    public String extendTransferKey(String key) {
        return TRANSFER_LOCK_PRIFIX + ":" + key;
    }

    /**
     * 装饰交易锁的key
     * @param key
     * @return
     */
    public String extendBusinessKey(String key) {
        return TRANSFER_LOCK_PRIFIX + ":" + key;
    }
}
