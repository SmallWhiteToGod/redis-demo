-- 解锁
if redis.call('get',KEYS[1]) == ARGV[1] then
     --如果为指定的锁，则直接删除(解锁)
   return redis.call('del',KEYS[1])
else --否则解锁失败，返回失败
   return 0
end