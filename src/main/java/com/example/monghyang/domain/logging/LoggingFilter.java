package com.example.monghyang.domain.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString();
        MDC.put("requestURI", request.getRequestURI());
        MDC.put("method", request.getMethod());
        MDC.put("traceId", traceId);
        MDC.put("clientIp", request.getRemoteAddr());

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            // 아래 두 개의 값은 인증 정보가 없을 시 null / Anonymous 로 설정된다.
            String userId = MDC.get("userId");
            String userRole = MDC.get("userRole");

            log.info("REQ_END traceId={} method={} path={} status={} durationMs={} userId={} roles={}",
                    traceId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    userId,
                    userRole);
            MDC.clear();
        }
    }
}
