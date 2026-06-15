package com.example.monghyang.domain.security.authHandler;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.logging.AuditLogger;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.util.JwtUtil;
import com.example.monghyang.domain.util.dto.JwtClaimsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    public static final String LOGOUT_USER_ID_ATTRIBUTE = CustomLogoutHandler.class.getName() + ".logoutUserId";
    public static final String LOGOUT_FAILURE_ATTRIBUTE = CustomLogoutHandler.class.getName() + ".logoutFailure";

    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    private final AuditLogger auditLogger;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = request.getHeader("X-Refresh-Token");
        if(refreshToken == null) {
            // 요청의 헤더에 refresh token이 존재하지 않으면 예외 발생
            auditLogger.logSecurityFailure("LOGOUT_FAILURE", request, null, null, ApplicationError.REFRESH_TOKEN_NOT_FOUND);
            throw new ApplicationException(ApplicationError.REFRESH_TOKEN_NOT_FOUND);
        }

        String tid = "";
        Long userId = null;

        try {
            JwtClaimsDto jwtClaimsDto = jwtUtil.parseRefreshToken(refreshToken);
            tid = jwtClaimsDto.getTid();
            userId = jwtClaimsDto.getUserId();
            request.setAttribute(LOGOUT_USER_ID_ATTRIBUTE, userId);

            if(userId != null) {
                redisService.deleteRefreshTokenAndSession(userId, tid); // 리프레시 토큰 삭제
            }
        } catch (ApplicationException e) {
            request.setAttribute(LOGOUT_FAILURE_ATTRIBUTE, Boolean.TRUE);
            auditLogger.logSecurityFailure("LOGOUT_FAILURE", request, userId, null, e.getApplicationError());
        } catch (Exception e) {
            request.setAttribute(LOGOUT_FAILURE_ATTRIBUTE, Boolean.TRUE);
            auditLogger.logOperationalError("LOGOUT_OPERATIONAL_ERROR", request, e.getClass().getSimpleName(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}
