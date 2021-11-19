package com.example.redis.redisson.impl;

import com.example.redis.redisson.AbstractLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 迁移锁服务
 */
@Service
public class TransferLockService extends AbstractLock {
    private final Logger logger = LoggerFactory.getLogger(TransferLockService.class);

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 加迁移锁(写锁)
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
        //迁移锁未设置过期时间,需要手动解锁
        lock.writeLock().lock();
        logger.info("迁移锁申请成功, key: [{}]", key);
        return true;
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
        if(lock.writeLock().isLocked() && lock.writeLock().isHeldByCurrentThread()) {
            try {
                lock.writeLock().unlock();
                logger.info("迁移锁解锁, key: [{}]", key);
            } catch (Exception e) {
                logger.error("迁移锁解锁异常, 请检查!!!, key: [{}]", key, e);
            }
        }
    }

}
