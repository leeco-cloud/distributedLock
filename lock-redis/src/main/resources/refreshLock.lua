if (redis.call('pttl', KEYS[1]) < ARGV[2]) then
    redis.call('pexpire', KEYS[1], ARGV[1]);
    return true;
end;
return false;