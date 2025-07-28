package com.example.monghyang.domain.redis;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final Long refreshTokenExpiration; // Redis 요소 수명
    private final Duration sessionExpiration;
    private final RedisTemplate<String, String> stringRedisTemplate; // access token tid 저장용 redis 템플릿
    private static final String ACCESS_TOKEN_TID_PREFIX = "access_token_tid:";
    @Autowired
    public RedisService(RedisTemplate<String, String> stringRedisTemplate, @Value("${jwt.expiration}") Long refreshTokenExpiration,
                        @Value("${spring.session.timeout}") Duration sessionExpiration) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.sessionExpiration = sessionExpiration;
    }

    private String createRefreshTokenKey(Long userId, String deviceType) {
        return "refresh:"+userId+":"+deviceType;
    }

    private String createLoginInfoKey(Long userId, String deviceType) {
        return "auth:"+userId+":"+deviceType;
    }

    // 로그인 세션 리스트 정보 저장
    public void setLoginInfoWithDeviceType(Long userId, String deviceType, String sessionId) {
        String key = createLoginInfoKey(userId, deviceType);
        System.out.println("session's user id: "+userId);
        System.out.println("session's user device type: "+deviceType);
        System.out.println("session id: "+sessionId);
        System.out.println("session expiration: "+sessionExpiration.toMillis());
        stringRedisTemplate.opsForValue().set(key, sessionId, sessionExpiration.toMillis(), TimeUnit.MILLISECONDS);
    }

    // 세션 리프레시 토큰 정보 저장
    public void setRefreshTokenTid(Long userId, String deviceType, String tid) {
        String key = createRefreshTokenKey(userId, deviceType);
        System.out.println("refresh token redis key: "+key);
        System.out.println("refresh token tid: "+tid);
        System.out.println("refresh token's user device type: "+deviceType);
        System.out.println("refresh token expiration: "+refreshTokenExpiration);
        stringRedisTemplate.opsForValue().set(key, tid, refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }

    // 회원 식별자, 디바이스 타입에 해당하는 기존 키의 value(sid)를 반환
    public String getSessionIdWithUserIdAndDeviceType(Long userId, String deviceType) {
        String key = createLoginInfoKey(userId, deviceType);
        return stringRedisTemplate.opsForValue().get(key);
    }


    // 유저의 refresh token tid 일치 여부 비교
    public boolean verifyRefreshTokenTid(Long userId, String deviceType, String tid) {
        String key = createRefreshTokenKey(userId, deviceType);
        String storedTid = stringRedisTemplate.opsForValue().get(key);
        if(storedTid == null){
            throw new ApplicationException(ApplicationError.CONCURRENT_CONNECTION);
        }
        return storedTid.equals(tid);
    }

    public void deleteSessionId(String sessionId) {
        String key = "spring:session:sessions:" + sessionId;
        stringRedisTemplate.delete(key);
    }

}
