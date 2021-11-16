package com.example.redis.service.impl;

import com.example.redis.service.ILockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 迁移锁服务
 */
@Service
public class TransferLockService implements ILockService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    static DefaultRedisScript<Long> tryTransferLockScript;
    static DefaultRedisScript<Long> unLockScript;

    //项目启动的时候直接加载Lua脚本
    static {
        //加载加锁的lua脚本
        tryTransferLockScript = new DefaultRedisScript<>();
        tryTransferLockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/transferLock.lua")));

        //加载解锁的lua脚本
        unLockScript = new DefaultRedisScript<>();
        unLockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/unlock.lua")));
        //设置返回值类型
        tryTransferLockScript.setResultType(Long.class);
        unLockScript.setResultType(Long.class);
    }

    /**
     * 加锁 并设置过期时间
     * @param key
     * @return
     */
    public Boolean tryLock(String key, String uuid) {
        List<String> keys = new ArrayList<>(2);
        keys.add(this.extendTransferKey(key));
        keys.add(this.extendBusinessKey(key));

        long result = stringRedisTemplate.execute(tryTransferLockScript, keys,
                uuid, TRANSFER_LOCK_EXPIRE_TIME);

        if (result == 1) {
            System.out.println(String.format("[%s]——迁移锁申请成功, key: [%s], uuid: [%s]",Thread.currentThread().getName(), key, uuid));
            return true;
        } else {
            System.out.println(String.format("[%s]——迁移锁申请失败, key: [%s], uuid: [%s]",Thread.currentThread().getName(), key, uuid));
            return false;
        }
    }

    /**
     * 解锁
     * @param key
     * @return
     */
    public Boolean unLock(String key, String uuid) {
        String transferKey = this.extendTransferKey(key);

        long result = stringRedisTemplate.execute(unLockScript, Collections.singletonList(transferKey),
                uuid);

        if (result == 1) {
            System.out.println(String.format("[%s]——迁移锁删除成功, key: [%s], uuid: [%s]",Thread.currentThread().getName(), key, uuid));
            return true;
        } else {
            System.out.println(String.format("[%s]——迁移锁删除失败, key: [%s], uuid: [%s]",Thread.currentThread().getName(), key, uuid));
            return false;
        }
    }

    /**
     * 对多个key同时加锁
     * @param keys
     * @param uuid
     * @return
     */
    @Override
    public Boolean tryLockList(List<String> keys, String uuid) {
        int num = 0;
        try {
            for (String key : keys) {
                boolean res = this.tryLock(key, uuid);
                num++;
                if (!res) {
                    break;
                }
            }
        } finally {
            //如果存在加锁失败的情况,就把已加过的锁释放
            if (num != keys.size()) {
                for (int i = 0; i < num; i++) {
                    this.unLock(keys.get(i), uuid);
                }
            }
        }
        return num == keys.size();
    }

    @Override
    public Boolean unLockList(List<String> keys, String uuid) {
        return null;
    }

    /**
     * 装饰迁移锁的key
     * @param key
     * @return
     */
    @Override
    public String extendTransferKey(String key) {
        return TRANSFER_LOCK_PRIFIX + ":" + key;
    }

    /**
     * 装饰交易锁的key
     * @param key
     * @return
     */
    @Override
    public String extendBusinessKey(String key) {
        return BUSINESS_LOCK_PRIFIX + ":" + key;
    }
}
