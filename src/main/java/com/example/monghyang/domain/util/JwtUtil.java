package com.example.monghyang.domain.util;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.redis.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {
    // jwt 토큰 생성 및 파싱을 담당하는 유틸 클래스
    private final SecretKey refreshKey; // refresh token 암호화 키
    private final Long refreshExpiration; // refresh token 수명
    private final RedisService redisService;

    public JwtUtil(@Value("${jwt.refresh-secret}") String refreshSecret, @Value("${jwt.refresh-expiration}") Long refreshExpiration
            , RedisService redisService) {

        this.refreshKey = new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        this.refreshExpiration = refreshExpiration;
        this.redisService = redisService;
    }

    // refresh token 파싱 메소드
    private Claims parseRefreshToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 파싱 예외 처리
            log.error("토큰 파싱 에러: {}", e.getMessage());
            throw new ApplicationException(ApplicationError.TOKEN_IMPAIRED);
        }
    }

    // Refresh token에서 디바이스 타입 추출
    public String getRefreshDeviceType(String token) {
        Claims claims = parseRefreshToken(token);
        return claims.get("deviceType", String.class);
    }

    // Refresh 토큰에서 유저 식별자 추출
    public Long getRefreshUserId(String token) {
        Claims claims = parseRefreshToken(token);
        return claims.get("userId", Long.class);
    }

    // Refresh 토큰에서 토큰 식별자(uuid) 추출
    public String getRefreshTid(String token) {
        Claims claims = parseRefreshToken(token);
        return claims.getId();
    }

    // refresh token 만료여부 검증
    public boolean isExpiredRefreshToken(String token) {
        Claims claims = parseRefreshToken(token);
        return claims.getExpiration().before(new Date());
    }

    // session refresh token 발급
    public String createRefreshToken(Long userId, String deviceType) {
        String refreshTokenId = UUID.randomUUID().toString();
        redisService.setRefreshTokenTid(userId, deviceType, refreshTokenId); // redis에 refresh token tid 저장
        return Jwts.builder()
                .claim("userId", userId)
                .claim("deviceType", deviceType)
                .setId(refreshTokenId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 특정 유저가 서버로 전송한 refresh token의 tid값이 기존의 tid 값(redis)과 일치하는지 검증
    // 만료 여부 검증
    public boolean verifyRefreshToken(HttpServletRequest request) {
        // 요청의 refresh token 추출
        String refreshToken = request.getHeader("X-Refresh-Token");
        if(refreshToken == null) {
            // refresh token 존재하지 않을 시 예외 발생
            throw new ApplicationException(ApplicationError.AUTH_INFO_NOT_FOUND);
        }

        if(isExpiredRefreshToken(refreshToken)) {
            // refresh token 만료 시 예외 발생
            throw new ApplicationException(ApplicationError.TOKEN_EXPIRED);
        }

        Claims claims = parseRefreshToken(refreshToken);
        return redisService.verifyRefreshTokenTid(claims.get("userId", Long.class), claims.get("deviceType", String.class), claims.getId());
    }
}
