package com.example.monghyang.domain.util;

import com.example.monghyang.domain.security.authHandler.SessionUserInfo;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionUtil {
    private final RedisService redisService;
    private final JwtUtil jwtUtil;

    // 새로운 세션 및 RT 생성
    public void createNewAuthInfo(HttpServletRequest request, HttpServletResponse response, Long userId, String role) {
        HttpSession session = request.getSession(true); // 기존에 존재하는 세션을 조회. 세션이 없다면 새로 생성(true)
        if(session == null) { // 세션이 생성되지 않은 경우 예외처리
            throw new ApplicationException(ApplicationError.SESSION_CREATE_ERROR);
        }

        response.setHeader("X-Session-Id", session.getId()); // 응답 http header에 세션 아이디 삽입

        // 세션에 유저 정보 저장(SessionUserInfo record)
        session.setAttribute("sessionUserInfo", new SessionUserInfo(userId, role));
        // 마지막 로그인 지역 정보 저장
        session.setAttribute("lastAccessLocation", request.getRemoteAddr());

        // 동일 유저의 세션 정보 개수가 5개를 초과하면 가장 오래전에 생성된 세션 정보를 제거한다. (구현 예정)

        String refreshToken = jwtUtil.createRefreshToken(userId, role, session.getId()); // redis에 refresh token 정보 저장
        response.setHeader("X-Refresh-Token", refreshToken); // 응답 헤더에 refresh token 첨부
    }
}
