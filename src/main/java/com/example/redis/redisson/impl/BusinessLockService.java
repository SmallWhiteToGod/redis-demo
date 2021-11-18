package com.example.redis.redisson.impl;

import com.example.redis.redisson.AbstractLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 交易锁服务
 */
@Service
public class BusinessLockService extends AbstractLock {
    private final Logger logger = LoggerFactory.getLogger(BusinessLockService.class);

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 加交易锁 (读锁)
     * @param key
     * @return
     * @throws InterruptedException
     */
    @Override
    public Boolean tryLock(String key) throws InterruptedException {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        RReadWriteLock lock = redissonClient.getReadWriteLock(this.appendPrefix(key));
        Boolean res = lock.readLock().tryLock(LOCK_WAIT_TIME, BUSINESS_LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        logger.info("[{}]——交易锁申请{}, key: [{}]",
                Thread.currentThread().getName(), res ? "成功" : "失败", key);
        return res;
    }

    /**
     * 解交易锁 (读锁)
     * @param key
     * @return
     */
    @Override
    public void unLock(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        RReadWriteLock lock = redissonClient.getReadWriteLock(this.appendPrefix(key));
        lock.readLock().unlock();
        logger.info("[{}]——交易锁解锁, key: [{}]", Thread.currentThread().getName(), key);
    }
}
