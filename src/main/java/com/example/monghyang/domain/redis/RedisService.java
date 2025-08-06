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
    private final Long sessionExpiration;
    private final RedisTemplate<String, String> stringRedisTemplate; // access token tid 저장용 redis 템플릿
    @Autowired
    public RedisService(RedisTemplate<String, String> stringRedisTemplate, @Value("${jwt.refresh-expiration}") Duration refreshTokenExpiration,
                        @Value("${spring.session.timeout}") Duration sessionExpiration) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.refreshTokenExpiration = refreshTokenExpiration.toMillis();
        this.sessionExpiration = sessionExpiration.toMillis();
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
        stringRedisTemplate.opsForValue().set(key, sessionId, sessionExpiration, TimeUnit.MILLISECONDS);
    }

    // 세션 리프레시 토큰 정보 저장
    public void setRefreshTokenTid(Long userId, String deviceType, String tid) {
        String key = createRefreshTokenKey(userId, deviceType);
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
            // 조회되는 것이 아무것도 없다면 토큰이 만료된 것 -> 재로그인 필요
            throw new ApplicationException(ApplicationError.TOKEN_EXPIRED);
        }
        return storedTid.equals(tid);
    }

    // 로그인 정보 ttl 갱신
    public void expireLoginInfo(Long userId, String deviceType) {
        String key = createLoginInfoKey(userId, deviceType);
        stringRedisTemplate.expire(key, sessionExpiration, TimeUnit.MILLISECONDS);
    }


    // 세션 제거
    public void deleteSessionId(String sessionId) {
        String key = "spring:session:sessions:" + sessionId;
        stringRedisTemplate.delete(key);
    }

    // 리프레시 토큰 제거
    public void deleteRefreshTokenTid(Long userId, String deviceType) {
        String key = createRefreshTokenKey(userId, deviceType);
        stringRedisTemplate.delete(key);
    }

    // 디바이스별 로그인 정보 제거
    public void deleteLoginInfo(Long userId, String deviceType) {
        String key = createLoginInfoKey(userId, deviceType);
        String sessionId = getSessionIdWithUserIdAndDeviceType(userId, deviceType);
        deleteSessionId(sessionId);
        stringRedisTemplate.delete(key);
    }

}
