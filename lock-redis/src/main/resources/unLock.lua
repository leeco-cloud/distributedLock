if (redis.call('exists', KEYS[1]) == 1) then
    redis.call('del', KEYS[1]);
    return true;
end;
return false;