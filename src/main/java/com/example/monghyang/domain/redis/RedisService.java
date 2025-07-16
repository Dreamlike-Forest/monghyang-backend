package com.example.monghyang.domain.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final Long expiration; // Redis 요소 수명
    private final RedisTemplate<String, String> stringRedisTemplate; // access token tid 저장용 redis 템플릿
    private static final String ACCESS_TOKEN_TID_PREFIX = "access_token_tid:";
    @Autowired
    public RedisService(RedisTemplate<String, String> stringRedisTemplate, @Value("${jwt.expiration}") Long expiration) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.expiration = expiration;
    }

    public void saveAccessTokenTid(Long userId, String tid) {
        String key = ACCESS_TOKEN_TID_PREFIX + userId;
        System.out.println("access token redis key: "+key);
        System.out.println("access token tid: "+tid);
        System.out.println("access token expiration: "+expiration);
        stringRedisTemplate.opsForValue().set(key, tid, expiration, TimeUnit.MILLISECONDS);
    }

    public String getAccessTokenTid(Long userId) {
        String key = ACCESS_TOKEN_TID_PREFIX + userId;
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 유저의 access token tid 일치 여부 비교
    public boolean verifyAccessTokenTid(Long userId, String tid) {
        String storedTid = getAccessTokenTid(userId);
        return storedTid.equals(tid);
    }

    public void deleteAccessTokenTid(Long userId) {
        String key = ACCESS_TOKEN_TID_PREFIX + userId;
        stringRedisTemplate.delete(key);
    }

}
