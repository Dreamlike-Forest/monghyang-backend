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
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
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
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    @Autowired
    public RedisService(RedisTemplate<String, String> stringRedisTemplate, @Value("${jwt.refresh-expiration}") Duration refreshTokenExpiration,
                        @Value("${spring.session.timeout}") Duration sessionExpiration,
                        FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.refreshTokenExpiration = refreshTokenExpiration.toMillis();
        this.sessionExpiration = sessionExpiration.toMillis();
        this.sessionRepository = sessionRepository;
    }

    private String createRefreshTokenKey(Long userId, String tid) {
        return "refresh:"+userId+":"+tid;
    }

    // 세션 리프레시 토큰 정보 저장
    public void setRefreshTokenTid(Long userId, String tid, String sessionId) {
        String key = createRefreshTokenKey(userId, tid);
        stringRedisTemplate.opsForValue().set(key, sessionId, refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }

    // 리프레시 토큰 및 세션 제거
    public void deleteRefreshTokenTid(Long userId, String tid) {
        String key = createRefreshTokenKey(userId, tid);
        String storedSid = stringRedisTemplate.opsForValue().get(key);
        if(storedSid != null){
            deleteSession(storedSid);
        }
        stringRedisTemplate.delete(key);
    }

    public void deleteSession(String sessionId) {
        if(sessionId == null || sessionId.isBlank()) {
            return;
        }
        try {
            sessionRepository.deleteById(sessionId);
        } catch (Exception e) {
            log.warn("세션 삭제 중 예외 발생. sessinoId={}", sessionId, e);
        }
    }

    /**
     * redis에서 특정 유저의 모든 세션 및 refresh token 정보 제거
     * @param userId 회원 식별자
     */
    public void deleteAllInfoByUserId(Long userId) {
        String refreshKeyPattern = "refresh:"+userId+":*";
        try(Cursor<String> cursor = stringRedisTemplate.scan(ScanOptions.scanOptions().match(refreshKeyPattern).count(5).build())) {
            while (cursor.hasNext()) {
                String curRefreshKey = cursor.next();
                String curSessionId = stringRedisTemplate.opsForValue().get(curRefreshKey);
                stringRedisTemplate.delete(curRefreshKey);
                deleteSession(curSessionId);
            }
        }
    }

}
