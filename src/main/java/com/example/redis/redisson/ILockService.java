package com.example.redis.redisson;

import java.util.List;

/**
 * 迁移锁与交易锁接口
 */
public interface ILockService {

    /**
     * 加锁
     * @param key
     * @return
     */
    Boolean tryLock(String key) throws InterruptedException;

    /**
     * 解锁
     * @param key
     * @return
     */
    void unLock(String key);

    /**
     * 对多个key同时加锁 todo
     * @param keys
     * @return
     */
    Boolean tryMultiLock(List<String> keys) throws InterruptedException;

    /**
     * 对多个key同时解锁 todo
     * @param keys
     * @return
     */
    void unMultiLock(List<String> keys);

    /**
     * 装饰key (添加锁前缀)
     * @param key
     * @return
     */
    String appendPrefix(String key);
}
