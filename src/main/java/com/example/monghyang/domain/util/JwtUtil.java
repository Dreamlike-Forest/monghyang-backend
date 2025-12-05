package com.example.monghyang.domain.util;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.util.dto.JwtClaimsDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {
    // jwt 토큰 생성 및 파싱을 담당하는 유틸 클래스
    private final SecretKey refreshKey; // refresh token 암호화 키
    private final Long refreshExpiration; // refresh token 수명
    private final RedisService redisService;

    public JwtUtil(@Value("${jwt.refresh-secret}") String refreshSecret, @Value("${jwt.refresh-expiration}") Duration refreshExpiration
            , RedisService redisService) {

        this.refreshKey = new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        this.refreshExpiration = refreshExpiration.toMillis();
        this.redisService = redisService;
    }

    // refresh token 파싱(dto에 필드값 담아서 반환)
    public JwtClaimsDto parseRefreshToken(String token) throws ApplicationException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tid = claims.getId();
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            if(!redisService.isExistRefreshToken(userId, tid) || claims.getExpiration().before(new Date())) {
                // 토큰 만료 시 예외 발생
                throw new ApplicationException(ApplicationError.TOKEN_EXPIRED);
            }

            return JwtClaimsDto.tidUserIdDeviceTypeRoleOf(tid, userId, role);
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 파싱 예외 처리
            log.error("토큰 훼손: {}", e.getMessage());
            throw new ApplicationException(ApplicationError.TOKEN_IMPAIRED);
        }
    }

    // session refresh token 발급
    public String createRefreshToken(Long userId, String role, String sessionId) {
        String refreshTokenId = UUID.randomUUID().toString();
        redisService.setRefreshTokenTid(userId, refreshTokenId, sessionId); // redis에 refresh token tid와 sid 저장
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setId(refreshTokenId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
