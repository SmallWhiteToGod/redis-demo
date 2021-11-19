package com.example.redis.redisson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 迁移锁和交易锁 抽象类
 */
public abstract class AbstractLock implements ILockService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    //锁前缀 交易锁和迁移锁在redis中用同一个key,以redisson的读写锁分别实现
    protected final static String LOCK_PREFIX = "transfer:lock";
    //加锁等待时间 单位ms
    protected final static Long LOCK_WAIT_TIME = 50L;
    //交易锁过期时间 单位ms
    protected final static Long BUSINESS_LOCK_EXPIRE_TIME = 60L * 1000L;

    /**
     * 加锁
     * @param key
     * @return
     */
    @Override
    public abstract Boolean tryLock(String key) throws InterruptedException;

    /**
     * 解锁
     * @param key
     * @return
     */
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
        boolean res = true;
        int count = -1;
        try {
            for (int i = 0; i < keys.size(); i++) {
                if (this.tryLock(keys.get(i))) {
                    count = i;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //如果不是所有的key都加锁成功 则把前面加锁的key解锁
            if (count != keys.size() - 1) {
                for (int i = 0; i <= count; i++) {
                    this.unLock(keys.get(i));
                }
                //返回失败
                res = false;
            }
            logger.info("联锁加锁[{}], keys:{}", res ? "成功" : "失败", Arrays.asList(keys.toArray()));
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

    /**
     * 添加锁前缀
     * @param key
     * @return
     */
    @Override
    public String appendPrefix(String key) {
        return LOCK_PREFIX + ":" +key;
    }
}
