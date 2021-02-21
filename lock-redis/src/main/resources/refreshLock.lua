if (redis.call('ttl', KEYS[1]) < tonumber(ARGV[2])) then
    redis.call('expire', KEYS[1], ARGV[1]);
    return true;
end;
return false;