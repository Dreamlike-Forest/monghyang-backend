package com.example.monghyang.domain.authHandler;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.util.DeviceTypeUtil;
import com.example.monghyang.domain.util.JwtUtil;
import com.example.monghyang.domain.util.dto.JwtClaimsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {
    private final RedisService redisService;
    private final JwtUtil jwtUtil;

    @Autowired
    public CustomLogoutHandler(RedisService redisService, JwtUtil jwtUtil) {
        this.redisService = redisService;
        this.jwtUtil = jwtUtil;
    }
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = request.getHeader("X-Refresh-Token");
        if(refreshToken == null) {
            // 요청의 헤더에 refresh token이 존재하지 않으면 예외 발생
            throw new ApplicationException(ApplicationError.REFRESH_TOKEN_NOT_FOUND);
        }

        String tid = "";
        Long userId = -1L;
        String deviceType = "";

        try {
            JwtClaimsDto jwtClaimsDto = jwtUtil.parseRefreshToken(refreshToken);
            tid = jwtClaimsDto.getTid();
            userId = jwtClaimsDto.getUserId();
            deviceType = jwtClaimsDto.getDeviceType();

            if(!redisService.verifyRefreshTokenTid(userId, deviceType, tid)) {
                // redis에 존재하지 않는 RT를 이용하여 로그아웃을 시도하는 경우, 서버에서는 아무런 조치를 취하지 않는다.
                return;
            }

            if(userId != null) {
                redisService.deleteLoginInfo(userId, deviceType); // 디바이스 별 로그인 정보 삭제
                redisService.deleteRefreshTokenTid(userId, deviceType); // 디바이스 별 리프레시 토큰 삭제
            }
        } catch (ApplicationException e) {
            log.error("토큰 관련 예외 발생. user id: {}, device type: {}\nerror message: {}", userId, deviceType, e.getMessage());
        } catch (Exception e) {
            log.error("로그아웃 redis 접근 중 에러 발생. user id: {}, device type: {}\nerror message: {}", userId, deviceType, e.getMessage());
        }



    }
}
