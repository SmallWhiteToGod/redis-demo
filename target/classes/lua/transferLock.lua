-- 加迁移锁
if redis.call('exists',KEYS[2]) == 1 then
    -- 如果存在交易锁 返回失败
    return 0
elseif redis.call('setnx',KEYS[1],ARGV[1]) == 1 then
    -- 加迁移锁成功 设置过期时间
    return redis.call('expire',KEYS[1],ARGV[2])
else -- 如果存在迁移锁 返回失败
    return 0
end