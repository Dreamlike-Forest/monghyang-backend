package com.example.monghyang.domain.authHandler;

import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.util.DeviceTypeUtil;
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

    @Autowired
    public CustomLogoutHandler(RedisService redisService) {
        this.redisService = redisService;
    }
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String deviceType = DeviceTypeUtil.getDeviceType(request).name();

        if(userId != null) {
            try {
                redisService.deleteLoginInfo(userId, deviceType); // 디바이스 별 로그인 정보 삭제
                redisService.deleteRefreshTokenTid(userId, deviceType); // 디바이스 별 리프레시 토큰 삭제
            } catch (Exception e) {
                log.error("로그아웃 redis 접근 중 에러 발생. user id: {}, device type: {}\nerror message: {}", userId, deviceType, e.getMessage());
            }
        }
    }
}
