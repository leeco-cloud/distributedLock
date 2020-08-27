if (redis.call('hexists', KEYS[1]) == 1) then
    redis.call('del', KEYS[1]);
    return true;
end;
if (redis.call('hexists', KEYS[1], ARGV[2]) > 1) then
    redis.call('hincrby', KEYS[1], ARGV[1], -1);
    return true;
end;
return false;