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
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SessionUtil {
    private static final int MAX_SESSIONS_PER_USER = 5;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    // 새로운 세션 및 RT 생성
    public void createNewAuthInfo(HttpServletRequest request, HttpServletResponse response, Long userId, String role) {
        String principal = userId.toString();
        Map<String, ? extends Session> sessionsByPrincipal =
                sessionRepository.findByIndexNameAndIndexValue(
                        FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                        principal);
        int currentSessionCount = sessionsByPrincipal.size();
        if(currentSessionCount >= MAX_SESSIONS_PER_USER) {
            int toDelete = currentSessionCount + 1 - MAX_SESSIONS_PER_USER;
            sessionsByPrincipal.values().stream()
                    .sorted(Comparator.comparing(Session::getCreationTime))
                    .limit(toDelete)
                    .forEach(session -> {
                        redisService.deleteRefreshTokenByUserIdAndSid(userId, session.getId()); // refresh token 제거
                        sessionRepository.deleteById(session.getId()); // 세션 제거
                    });
        }


        HttpSession session = request.getSession(true); // 기존에 존재하는 세션을 조회. 세션이 없다면 새로 생성(true)
        if(session == null) { // 세션이 생성되지 않은 경우 예외처리
            throw new ApplicationException(ApplicationError.SESSION_CREATE_ERROR);
        }

        response.setHeader("X-Session-Id", session.getId()); // 응답 http header에 세션 아이디 삽입

        // 세션에 유저 정보 저장(SessionUserInfo record)
        session.setAttribute("sessionUserInfo", new SessionUserInfo(userId, role));
        // 마지막 로그인 지역 정보 저장
        session.setAttribute("lastAccessLocation", request.getRemoteAddr());

        // index key 설정
        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                userId.toString());

        String refreshToken = jwtUtil.createRefreshToken(userId, role, session.getId()); // redis에 refresh token 정보 저장
        response.setHeader("X-Refresh-Token", refreshToken); // 응답 헤더에 refresh token 첨부
    }
}
