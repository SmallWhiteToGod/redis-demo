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
 * 交易锁服务
 */
@Service
@Deprecated
public class BusinessLockService implements ILockService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    static DefaultRedisScript<Long> tryBusinessLockScript;
    static DefaultRedisScript<Long> unLockScript;

    //项目启动的时候直接加载Lua脚本
    static {
        //加载加锁的lua脚本
        tryBusinessLockScript = new DefaultRedisScript<>();
        tryBusinessLockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/businessLock.lua")));

        //加载解锁的lua脚本
        unLockScript = new DefaultRedisScript<>();
        unLockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/unlock.lua")));
        //设置返回值类型
        tryBusinessLockScript.setResultType(Long.class);
        unLockScript.setResultType(Long.class);
    }

    /**
     * 加锁 并设置过期时间
     * @param key
     * @return
     */
    public Boolean tryLock(String key, String uuid) {
        List<String> keys = new ArrayList<>(2);
        keys.add(this.extendBusinessKey(key));
        keys.add(this.extendTransferKey(key));

        long result = stringRedisTemplate.execute(tryBusinessLockScript, keys,
                uuid, BUSINESS_LOCK_EXPIRE_TIME);

        if (result == 1) {
            System.out.println(String.format("[%s]——交易锁申请成功, key: [%s}], uuid: [%s]",Thread.currentThread().getName(), key, uuid));
            return true;
        } else {
            System.out.println(String.format("[%s]——交易锁申请失败, key: [%s}], uuid: [%s]",Thread.currentThread().getName(), key, uuid));
            return false;
        }
    }

    /**
     * 解锁
     * @param key
     * @return
     */
    public Boolean unLock(String key, String uuid) {
        String transferKey = this.extendBusinessKey(key);

        long result = stringRedisTemplate.execute(unLockScript, Collections.singletonList(transferKey),
                uuid);

        if (result == 1) {
            System.out.println(String.format("[%s]——交易锁删除成功, key: [%s}], uuid: [%s]",Thread.currentThread().getName(), key, uuid));
            return true;
        } else {
            System.out.println(String.format("[%s]——交易锁删除失败, key: [%s}], uuid: [%s]",Thread.currentThread().getName(), key, uuid));
            return false;
        }
    }

    @Override
    public Boolean tryLockList(List<String> keys, String value) {
        return null;
    }

    @Override
    public Boolean unLockList(List<String> keys, String value) {
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
