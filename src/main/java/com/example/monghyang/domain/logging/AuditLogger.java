package com.example.monghyang.domain.logging;

import com.example.monghyang.domain.global.advice.ApplicationError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 감사 로그와 운영 오류 로그의 구조화 필드를 일관되게 기록합니다.
 *
 * <p>토큰, 비밀번호, 쿠키, 세션 식별자 같은 민감값은 기록하지 않습니다.
 * 요청 단위 중복 로그를 줄이기 위해 request attribute에 기록 여부를 표시합니다.</p>
 */
@Component
@Slf4j
public class AuditLogger {
    public static final String SECURITY_EVENT_LOGGED_ATTRIBUTE = AuditLogger.class.getName() + ".securityEventLogged";
    public static final String OPERATIONAL_ERROR_LOGGED_ATTRIBUTE = AuditLogger.class.getName() + ".operationalErrorLogged";
    public static final String DOMAIN_WRITE_LOGGED_ATTRIBUTE = AuditLogger.class.getName() + ".domainWriteLogged";

    /**
     * 인증/인가 또는 중요 계정 작업의 성공 이벤트를 감사 로그로 기록합니다.
     *
     * @param eventName 감사 이벤트 이름
     * @param request 현재 요청
     * @param userId 회원 식별자
     * @param userRole 회원 권한
     */
    public void logSecuritySuccess(String eventName, HttpServletRequest request, Long userId, String userRole) {
        Map<String, String> fields = baseFields("SECURITY", eventName, "SUCCESS", request);
        putIfNotNull(fields, "userId", userId);
        putIfNotBlank(fields, "userRole", userRole);
        mark(request, SECURITY_EVENT_LOGGED_ATTRIBUTE);
        withMdc(fields, () -> log.info("SECURITY_AUDIT"));
    }

    /**
     * 인증/인가 또는 중요 계정 작업의 실패 이벤트를 감사 로그로 기록합니다.
     *
     * @param eventName 감사 이벤트 이름
     * @param request 현재 요청
     * @param userId 확인된 회원 식별자. 알 수 없으면 null
     * @param userRole 확인된 회원 권한. 알 수 없으면 null
     * @param error 실패 원인으로 매핑된 애플리케이션 오류
     */
    public void logSecurityFailure(String eventName, HttpServletRequest request, Long userId, String userRole, ApplicationError error) {
        Map<String, String> fields = baseFields("SECURITY", eventName, "FAILURE", request);
        putIfNotNull(fields, "userId", userId);
        putIfNotBlank(fields, "userRole", userRole);
        if (error != null) {
            fields.put("errorName", error.name());
            fields.put("httpStatus", String.valueOf(error.getStatus().value()));
        }
        mark(request, SECURITY_EVENT_LOGGED_ATTRIBUTE);
        withMdc(fields, () -> log.warn("SECURITY_AUDIT"));
    }

    /**
     * 운영자가 조사해야 하는 서버 오류 또는 외부 연동 실패를 기록합니다.
     *
     * @param eventName 운영 오류 이벤트 이름
     * @param request 현재 요청
     * @param errorName 오류 코드 또는 예외 분류명
     * @param httpStatus 응답 HTTP 상태 코드
     * @param throwable 원본 예외
     */
    public void logOperationalError(String eventName, HttpServletRequest request, String errorName, int httpStatus, Throwable throwable) {
        Map<String, String> fields = baseFields("OPERATIONAL", eventName, "FAILURE", request);
        putIfNotBlank(fields, "errorName", errorName);
        fields.put("httpStatus", String.valueOf(httpStatus));
        mark(request, OPERATIONAL_ERROR_LOGGED_ATTRIBUTE);
        withMdc(fields, () -> log.error("OPERATIONAL_ERROR", throwable));
    }

    /**
     * 양조장 또는 판매자 쓰기 작업의 응답 결과를 감사 로그로 기록합니다.
     *
     * @param ownerType 작업 주체 유형. BREWERY 또는 SELLER
     * @param request 현재 요청
     * @param httpStatus 응답 HTTP 상태 코드
     * @param durationMs 요청 처리 시간
     */
    public void logDomainWriteResult(String ownerType, HttpServletRequest request, int httpStatus, long durationMs) {
        String normalizedOwnerType = ownerType == null ? "UNKNOWN" : ownerType;
        Map<String, String> fields = baseFields("DOMAIN_WRITE", normalizedOwnerType + "_WRITE_RESULT", outcomeOf(httpStatus), request);
        fields.put("ownerType", normalizedOwnerType);
        fields.put("httpStatus", String.valueOf(httpStatus));
        fields.put("durationMs", String.valueOf(durationMs));
        mark(request, DOMAIN_WRITE_LOGGED_ATTRIBUTE);
        withMdc(fields, () -> {
            if (httpStatus >= 500) {
                log.error("DOMAIN_WRITE_RESULT");
            } else if (httpStatus >= 400) {
                log.warn("DOMAIN_WRITE_RESULT");
            } else {
                log.info("DOMAIN_WRITE_RESULT");
            }
        });
    }

    private Map<String, String> baseFields(String category, String eventName, String outcome, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("eventCategory", category);
        fields.put("eventName", eventName);
        fields.put("eventOutcome", outcome);
        if (request != null) {
            putIfNotBlank(fields, "requestURI", request.getRequestURI());
            putIfNotBlank(fields, "method", request.getMethod());
            putIfNotBlank(fields, "clientIp", request.getRemoteAddr());
        }
        return fields;
    }

    private void withMdc(Map<String, String> fields, Runnable logAction) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        try {
            fields.forEach(MDC::put);
            logAction.run();
        } finally {
            if (previous == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(previous);
            }
        }
    }

    private void mark(HttpServletRequest request, String attributeName) {
        if (request != null) {
            request.setAttribute(attributeName, Boolean.TRUE);
        }
    }

    private void putIfNotNull(Map<String, String> fields, String key, Object value) {
        if (value != null) {
            fields.put(key, String.valueOf(value));
        }
    }

    private void putIfNotBlank(Map<String, String> fields, String key, String value) {
        if (value != null && !value.isBlank()) {
            fields.put(key, value);
        }
    }

    private String outcomeOf(int httpStatus) {
        return httpStatus >= 400 ? "FAILURE" : "SUCCESS";
    }
}
