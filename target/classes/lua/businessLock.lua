-- 加交易锁
if redis.call('exists',KEYS[2]) == 1 then
    -- 如果存在迁移锁 返回失败
    return 0
else
    -- 加交易锁 设置过期时间
    redis.call('set',KEYS[1],ARGV[1])
    return redis.call('expire',KEYS[1],ARGV[2])
end