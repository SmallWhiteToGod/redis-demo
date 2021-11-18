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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 迁移锁服务
 */
@Service
public class TransferLockService extends AbstractLock {

    private final Logger logger = LoggerFactory.getLogger(TransferLockService.class);

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 加迁移锁 (写锁)
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
        Boolean res = lock.writeLock().tryLock(LOCK_WAIT_TIME, TRANSFER_LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        logger.info("[{}]——迁移锁申请{}, key: [{}]",
                Thread.currentThread().getName(), res ? "成功" : "失败", key);
        return res;
    }

    /**
     * 解迁移锁 (写锁)
     * @param key
     * @return
     */
    @Override
    public void unLock(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        RReadWriteLock lock = redissonClient.getReadWriteLock(this.appendPrefix(key));
        lock.writeLock().unlock();
        logger.info("[{}]——迁移锁解锁, key: [{}]", Thread.currentThread().getName(), key);
    }

}
