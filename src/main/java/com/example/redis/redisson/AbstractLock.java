package com.example.redis.redisson;

import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 迁移锁和交易锁 抽象类
 */
public abstract class AbstractLock implements ILockService {
    //锁前缀 交易锁和迁移锁在redis中用同一个key,以redisson的读写锁分别实现
    protected final static String LOCK_PREFIX = "transfer:lock";
    //加锁等待时间 单位ms
    protected final static Long LOCK_WAIT_TIME = 50L;
    //迁移锁过期时间 单位ms
    protected final static Long TRANSFER_LOCK_EXPIRE_TIME = 60L * 1000L;
    //交易锁过期时间 单位ms
    protected final static Long BUSINESS_LOCK_EXPIRE_TIME = 60L * 1000L;

    @Override
    public abstract Boolean tryLock(String key) throws InterruptedException;

    @Override
    public abstract void unLock(String key);

    /**
     * 对多个key加迁移锁
     * @param keys
     * @return
     */
    @Override
    public Boolean tryMultiLock(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return false;
        }
        boolean res = false;
        int count = -1;
        try {
            for (int i = 0; i < keys.size(); i++) {
                if (this.tryLock(keys.get(i))) {
                    count = i;
                }
            }
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //如果不是所有的key都加锁成功 则把前面加锁的key解锁
            if (count != keys.size() - 1) {
                for (int i = 0; i <= count; i++) {
                    this.unLock(keys.get(i));
                }
            }
        }
        return res;
    }

    /**
     * 对多个key解迁移锁
     * @param keys
     * @return
     */
    @Override
    public void unMultiLock(List<String> keys) {
        for (String key : keys) {
            this.unLock(key);
        }
    }

    @Override
    public String appendPrefix(String key) {
        return LOCK_PREFIX + ":" +key;
    }
}
