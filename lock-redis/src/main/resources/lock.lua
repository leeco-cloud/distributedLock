if (redis.call('exists', KEYS[1]) == 0) then
    redis.call('set', KEYS[1], ARGV[1]);
    redis.call('expire', KEYS[1], ARGV[2]);
    return true;
end;
return false;