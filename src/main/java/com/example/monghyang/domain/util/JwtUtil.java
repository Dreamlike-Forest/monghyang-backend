package com.example.monghyang.domain.util;

import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtUtil {
    // jwt 토큰 생성 및 파싱을 담당하는 유틸 클래스
    private final SecretKey secretKey;
    private final Long expiration;
    private final SecretKey refreshKey;
    private final Long refreshExpiration;
    private final UsersRepository usersRepository;
    @Value("${app.cookie-samesite}")
    private String cookieSamesite;
    @Value("${app.cookie-domain}")
    private String cookieDomain;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") Long expiration
            , @Value("${jwt.refresh-secret}") String refreshSecret, @Value("${jwt.refresh-expiration}") Long refreshExpiration
            , UsersRepository usersRepository) {

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        this.refreshKey = new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
        this.usersRepository = usersRepository;

    }

    // 토큰을 SecretKey로 파싱하여 Claims를 얻어내는 메소드
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims parseRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
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

    // access token 발급
    public ResponseCookie createAccessToken(Long userId, String role) {
        String access = Jwts.builder()
                .claim("userId", userId) // userId
                .claim("role", role)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return ResponseCookie.from("access_token", access)
                .httpOnly(true).secure(true).sameSite(cookieSamesite)
                .domain(cookieDomain).path("/")
                .maxAge(expiration/1000).build();
    }

    // refresh token 발급
    public ResponseCookie createRefreshToken(Long userId, String role) {
        String refresh = Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();

        return ResponseCookie.from("refresh_token", refresh)
                .httpOnly(true).secure(true).sameSite(cookieSamesite)
                .domain(cookieDomain).path("/api/auth/refresh")
                .maxAge(refreshExpiration/1000).build();
    }

    // 특정 유저가 서버로 전송한 refresh token값이 해당 유저의 테이블의 refresh token 값과 일치하는지 검증
    public boolean isExistRefreshToken(Long userId, String refreshToken) {
        Optional<Users> user = usersRepository.findById(userId);
        if(user.isEmpty()) {
            return false;
        }

        return user.get().getRefreshToken().equals(refreshToken);
    }

    public ResponseCookie createMax0AccessToken() { // 수명이 0인 access token 발급: 로그아웃 용
        return ResponseCookie.from("access_token", "")
                .httpOnly(true).secure(true).sameSite(cookieSamesite)
                .domain(cookieDomain).path("/")
                .maxAge(Duration.ZERO).build();
    }

    public ResponseCookie createMax0RefreshToken() { // 수명이 0인 refresh token 발급: 로그아웃 용
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(true).sameSite(cookieSamesite)
                .domain(cookieDomain).path("/api/auth/refresh")
                .maxAge(Duration.ZERO).build();
    }

}
