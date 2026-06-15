package com.example.monghyang.domain.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 양조장 및 판매자 전용 쓰기 요청의 응답 결과를 감사 로그로 기록합니다.
 *
 * <p>조회 요청과 일반 사용자 쓰기 요청은 이 필터의 기록 대상이 아닙니다.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class DomainWriteLoggingFilter extends OncePerRequestFilter {
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final String BREWERY_PRIV_PREFIX = "/api/brewery-priv";
    private static final String SELLER_PRIV_PREFIX = "/api/seller-priv";

    private final AuditLogger auditLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            String ownerType = ownerTypeOf(request);
            if (ownerType != null && WRITE_METHODS.contains(request.getMethod())) {
                long durationMs = System.currentTimeMillis() - startTime;
                auditLogger.logDomainWriteResult(ownerType, request, response.getStatus(), durationMs);
            }
        }
    }

    private String ownerTypeOf(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(BREWERY_PRIV_PREFIX)) {
            return "BREWERY";
        }
        if (requestURI.startsWith(SELLER_PRIV_PREFIX)) {
            return "SELLER";
        }
        return null;
    }
}
