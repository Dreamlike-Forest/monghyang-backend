package com.example.monghyang.domain.util;

import com.example.monghyang.domain.authHandler.SessionUserInfo;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionUtil {
    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    @Autowired
    public SessionUtil(RedisService redisService, JwtUtil jwtUtil) {
        this.redisService = redisService;
        this.jwtUtil = jwtUtil;
    }

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

        String deviceType = DeviceTypeUtil.getDeviceType(request).name(); // 로그인을 시도한 클라이언트의 디바이스 타입

        // 동일한 계정, 디바이스 타입의 로그인 정보가 이미 있는 경우, 기존 SID를 삭제하고 새로운 데이터로 덮어씌운다.(디바이스 별 세션 정보)
        String storedSessionId = redisService.getSessionIdWithUserIdAndDeviceType(userId, deviceType);
        if(storedSessionId != null) {
            redisService.deleteSessionId(storedSessionId);
        }

        redisService.setLoginInfoWithDeviceType(userId, deviceType, session.getId()); // redis에 사용자 로그인 세션 정보 리스트 저장

        String refreshToken = jwtUtil.createRefreshToken(userId, deviceType, role); // redis에 refresh token 정보 저장
        response.setHeader("X-Refresh-Token", refreshToken); // 응답 헤더에 refresh token 첨부
    }
}
