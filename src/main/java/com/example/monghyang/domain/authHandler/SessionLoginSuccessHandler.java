package com.example.monghyang.domain.authHandler;

import com.example.monghyang.domain.filter.LoginDto;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.users.details.LoginUserDetails;
import com.example.monghyang.domain.util.DeviceTypeUtil;
import com.example.monghyang.domain.util.ExceptionUtil;
import com.example.monghyang.domain.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class SessionLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final JwtUtil jwtUtil;

    @Autowired
    public SessionLoginSuccessHandler(ObjectMapper objectMapper, RedisService redisService, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.redisService = redisService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        HttpSession session = request.getSession(true); // 기존에 존재하는 세션을 조회. 세션이 없다면 새로 생성(true)
        if(session == null) { // 세션이 생성되지 않은 경우 예외처리
            ExceptionUtil.filterExceptionHandler(response, ApplicationError.SESSION_CREATE_ERROR);
            return;
        }

        response.setHeader("X-Session-Id", session.getId()); // 응답 http header에 세션 아이디 삽입

        // 인증 객체에서 로그인 유저 정보 추출
        LoginUserDetails loginUserDetails = (LoginUserDetails) authentication.getPrincipal();
        String nickname = loginUserDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = loginUserDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        String role = iterator.next().getAuthority();
        Long userId = loginUserDetails.getUserId();

        // 세션에 유저 정보 저장(SessionUserInfo record)
        session.setAttribute("sessionUserInfo", new SessionUserInfo(userId, role));
        // 마지막 로그인 지역 정보 저장
        session.setAttribute("lastAccessLocation", request.getRemoteAddr());

        String deviceType = DeviceTypeUtil.getDeviceType(request).name(); // 로그인을 시도한 클라이언트의 디바이스 타입

        // 동일한 계정, 디바이스 타입의 로그인 세션 정보가 이미 있는 경우, 기존 SID를 삭제하고 새로운 데이터로 덮어씌운다.(세션 정보)
        String storedSessionId = redisService.getSessionIdWithUserIdAndDeviceType(userId, deviceType);
        if(storedSessionId != null) {
            redisService.deleteSessionId(storedSessionId);
        }

        redisService.setLoginInfoWithDeviceType(userId, deviceType, session.getId()); // redis에 사용자 로그인 세션 정보 리스트 저장

        String refreshToken = jwtUtil.createRefreshToken(userId, deviceType); // redis에 refresh token 정보 저장
        response.setHeader("X-Refresh-Token", refreshToken); // 응답 헤더에 refresh token 첨부

        // 응답 http body 작성
        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), LoginDto.nicknameRoleOf(nickname, role)); // 유저 닉네임, 권한 정보 반환(json)
    }
}
