package com.example.monghyang.domain.util;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.users.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
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
    private final SecretKey secretKey; // access token 암호화 키
    private final Long expiration; // access token 수명
    private final SecretKey refreshKey; // refresh token 암호화 키
    private final Long refreshExpiration; // refresh token 수명
    private final String cookieSamesite; // 쿠키의 same site 설정
    private final String cookieDomain; // 쿠키의 domain 설정
    private final String accessTokenCookieName; // access token 쿠키 이름
    private final String refreshTokenCookieName; // refresh token 쿠키 이름
    private final RedisService redisService;
    private final UsersRepository usersRepository;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") Long expiration
            , @Value("${jwt.refresh-secret}") String refreshSecret, @Value("${jwt.refresh-expiration}") Long refreshExpiration
            , @Value("${app.cookie-samesite}") String cookieSamesite, @Value("${app.cookie-domain}") String cookieDomain
            , @Value("${jwt.access-token-cookie-name}") String accessTokenCookieName , @Value("${jwt.refresh-token-cookie-name}") String refreshTokenCookieName
            , RedisService redisService, UsersRepository usersRepository) {

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        this.refreshKey = new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
        this.cookieSamesite = cookieSamesite;
        this.cookieDomain = cookieDomain;
        this.accessTokenCookieName = accessTokenCookieName;
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.redisService = redisService;
        this.usersRepository = usersRepository;
    }

    // 토큰을 SecretKey로 파싱하여 Claims를 얻어내는 메소드
    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 파싱 예외 처리
            log.error("토큰 파싱 에러: {}", e.getMessage());
            throw new ApplicationException(ApplicationError.TOKEN_IMPAIRED);
        }
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

    // 토큰에서 유저 식별자 추출
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    // 토큰에서 유저 권한 추출
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    // 토큰에서 토큰 식별자(uuid) 추출
    public String getId(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    // Refresh 토큰에서 유저 식별자 추출
    public Long getRefreshUserId(String token) {
        Claims claims = parseRefreshToken(token);
        return claims.get("userId", Long.class);
    }

    // Refresh 토큰에서 유저 권한 추출
    public String getRefreshRole(String token) {
        Claims claims = parseRefreshToken(token);
        return claims.get("role", String.class);
    }

    // Refresh 토큰에서 토큰 식별자(uuid) 추출
    public String getRefreshId(String token) {
        Claims claims = parseRefreshToken(token);
        return claims.getId();
    }

    // 토큰 만료여부 검증
    public boolean isExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

    // refresh token 만료여부 검증
    public boolean isExpiredRefreshToken(String token) {
        Claims claims = parseRefreshToken(token);
        return claims.getExpiration().before(new Date());
    }

    // access token 발급
    public ResponseCookie createAccessToken(Long userId, String role) {
        String accessTokenId = UUID.randomUUID().toString();
        String access = Jwts.builder()
                .claim("userId", userId) // userId
                .claim("role", role)
                .setId(accessTokenId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        redisService.saveAccessTokenTid(userId, accessTokenId); // redis에 access token tid 저장
        return ResponseCookie.from(accessTokenCookieName, access)
                .httpOnly(true).secure(true).sameSite(cookieSamesite)
                .domain(cookieDomain).path("/")
                .maxAge(expiration/1000).build();
    }

    // refresh token 발급
    public ResponseCookie createRefreshToken(Long userId, String role) {
        String refreshTokenId = UUID.randomUUID().toString();
        String refresh = Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setId(refreshTokenId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
        return ResponseCookie.from(refreshTokenCookieName, refresh)
                .httpOnly(true).secure(true).sameSite(cookieSamesite)
                .domain(cookieDomain).path("/api/auth/refresh")
                .maxAge(refreshExpiration/1000).build();
    }

    // 특정 유저가 서버로 전송한 refresh token의 tid값이 해당 유저의 테이블의 refresh token tid 값과 일치하는지 검증
    // 만료 여부 검증
    public boolean verifyRefreshToken(HttpServletRequest request, String prevRefreshTokenId) {
        // 요청의 refresh token 추출
        String refreshToken = getTokenFromRequest(request, refreshTokenCookieName);
        if(refreshToken == null) {
            // refresh token 존재하지 않을 시 예외 발생
            throw new ApplicationException(ApplicationError.AUTH_INFO_NOT_FOUND);
        }

        // 기존에 저장된 refresh token tid와 토큰 갱신 요청에 담긴 refresh token의 tid가 동일한지 검증
        if(!getRefreshId(refreshToken).equals(prevRefreshTokenId)) {
            // 다르다면 동시접속 발생한 것으로 간주
            throw new ApplicationException(ApplicationError.CONCURRENT_CONNECTION);
        }

        if(isExpiredRefreshToken(refreshToken)) {
            // refresh token 만료 시 예외 발생
            throw new ApplicationException(ApplicationError.TOKEN_EXPIRED);
        }
        return true;
    }

    public ResponseCookie createMax0AccessToken() { // 수명이 0인 access token 발급: 로그아웃 용
        return ResponseCookie.from(accessTokenCookieName, "")
                .httpOnly(true).secure(true).sameSite(cookieSamesite)
                .domain(cookieDomain).path("/")
                .maxAge(Duration.ZERO).build();
    }

    public ResponseCookie createMax0RefreshToken() { // 수명이 0인 refresh token 발급: 로그아웃 용
        return ResponseCookie.from(refreshTokenCookieName, "")
                .httpOnly(true).secure(true).sameSite(cookieSamesite)
                .domain(cookieDomain).path("/api/auth/refresh")
                .maxAge(Duration.ZERO).build();
    }

    // Http Request에서 특정 토큰을 찾아주는 메소드
    public String getTokenFromRequest(HttpServletRequest request, String tokenName) {
        String token = null;

        // 요청에 쿠키가 존재하는지 확인
        if(request.getCookies() != null) {
            // 쿠키에서 token을 찾는 과정
            for(Cookie cookie : request.getCookies()) {
                if(cookie.getName().equals(tokenName)) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        return token;
    }

    public String getAccessTokenCookieName() {
        return accessTokenCookieName;
    }
}
