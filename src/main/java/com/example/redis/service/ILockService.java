package com.example.redis.service;


import java.util.List;

public interface ILockService {
    //迁移锁前缀
    public final static String TRANSFER_LOCK_PRIFIX = "lock:t";
    //交易锁前缀
    public final static String BUSINESS_LOCK_PRIFIX = "lock:b";
    //迁移锁过期时间 单位s
    public final static String TRANSFER_LOCK_EXPIRE_TIME = "60";
    //交易锁过期时间 单位s
    public final static String BUSINESS_LOCK_EXPIRE_TIME = "1";

    /**
     * 加锁(并设置过期时间)
     * @param key
     * @param value
     * @return
     */
    Boolean tryLock(String key, String value);

    /**
     * 解锁
     * @param key
     * @param value
     * @return
     */
    Boolean unLock(String key, String value);

    /**
     * 对多个key同时加锁 todo
     * @param keys
     * @param value
     * @return
     */
    Boolean tryLockList(List<String> keys, String value);

    /**
     * 对多个key同时解锁 todo
     * @param keys
     * @param value
     * @return
     */
    Boolean unLockList(List<String> keys, String value);

    /**
     * 装饰迁移锁的key
     * @param key
     * @return
     */
    String extendTransferKey(String key);

    /**
     * 装饰交易锁key
     * @param key
     * @return
     */
    String extendBusinessKey(String key);
}
