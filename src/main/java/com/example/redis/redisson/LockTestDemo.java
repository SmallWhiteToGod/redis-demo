package com.example.redis.redisson;

import com.example.redis.redisson.impl.BusinessLockService;
import com.example.redis.redisson.impl.TransferLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class LockTestDemo {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BusinessLockService businessLockService;
    @Autowired
    private TransferLockService transferLockService;

    public void business(String custUid) {
        try {
            logger.info("开始进行交易, 客户号[{}]", custUid);
            //加交易锁
            if (businessLockService.tryLock(custUid)) {
                //睡眠一秒 模拟交易时间
                Thread.sleep(1000);
                logger.info("交易成功, 客户号[{}]", custUid);
            } else {
                logger.info("交易申请失败, 客户号[{}]", custUid);
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //解交易锁
            businessLockService.unLock(custUid);
        }
    }

    public void transfer(String custUid) {
        try {
            logger.info("开始进行迁移, 客户号[{}]", custUid);
            //加迁移锁
            if (transferLockService.tryLock(custUid)) {
                //睡眠一秒 模拟迁移时间
                Thread.sleep(1000);
                logger.info("迁移成功, 客户号[{}]", custUid);
            } else {
                logger.info("迁移申请失败, 客户号[{}]", custUid);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //解迁移锁
            transferLockService.unLock(custUid);
        }
    }

    public void companyBusiness(String[] custUids) {
        try {
            logger.info("开始进行公司卡交易, 客户号:{}", Arrays.toString(custUids));
            if (businessLockService.tryMultiLock(Arrays.asList(custUids))) {
                //睡眠2s 模拟交易时间
                Thread.sleep(2000);
                logger.info("公司卡交易成功, 客户号:{}", Arrays.toString(custUids));
            } else {
                logger.info("公司卡交易申请失败, 客户号:{}", Arrays.toString(custUids));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //解锁
            businessLockService.unMultiLock(Arrays.asList(custUids));
        }
    }

    public void companyTransfer(String[] custUids) {
        try {
            logger.info("开始进行公司卡迁移, 客户号:{}", Arrays.toString(custUids));
            if (transferLockService.tryMultiLock(Arrays.asList(custUids))) {
                //睡眠2s 模拟迁移时间
                Thread.sleep(2000);
                logger.info("公司卡迁移完成, 客户号:{}", Arrays.toString(custUids));
            } else {
                logger.info("公司卡迁移申请失败, 客户号:{}", Arrays.toString(custUids));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //解锁
            transferLockService.unMultiLock(Arrays.asList(custUids));
        }
    }
}
