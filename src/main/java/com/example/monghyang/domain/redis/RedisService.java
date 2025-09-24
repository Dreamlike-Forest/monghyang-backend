package com.example.monghyang.domain.redis;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.util.DeviceTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
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

    public void deleteAllInfo(Long userId) {
        int maxInfoNum = DeviceTypeUtil.DeviceType.values().length; // 한 유저가 가질 수 있는 최대 로그인 상태의 개수

        // 해당 유저의 모든 세션 제거
        String loginInfoKeyPattern = "auth:"+userId+":*";
        try(Cursor<String> cursor = stringRedisTemplate.scan(ScanOptions.scanOptions().match(loginInfoKeyPattern).count(maxInfoNum).build())) {
            while (cursor.hasNext()) {
                String curLoginInfoKey = cursor.next();
                String curSessionId = "spring:session:sessions:" + stringRedisTemplate.opsForValue().get(curLoginInfoKey);
                stringRedisTemplate.delete(curSessionId);
                stringRedisTemplate.delete(curLoginInfoKey);
            }
        }

        // 해당 유저의 모든 RT 제거
        String refreshTokenKeyPattern = "refresh:"+userId+":*";
        try(Cursor<String> cursor = stringRedisTemplate.scan(ScanOptions.scanOptions().match(refreshTokenKeyPattern).count(maxInfoNum).build())) {
            while (cursor.hasNext()) {
                String curRefreshTokenKey = cursor.next();
                if(curRefreshTokenKey != null){
                    stringRedisTemplate.delete(curRefreshTokenKey);
                }
            }
        }

    }

}
