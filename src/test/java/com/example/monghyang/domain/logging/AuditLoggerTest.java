package com.example.monghyang.domain.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLoggerTest {
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(AuditLogger.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        auditLogger = new AuditLogger();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("보안 성공 감사 로그는 이벤트명, 결과, 사용자, 요청 정보를 MDC에 포함한다")
    void log_security_success_writes_structured_mdc() {
        MockHttpServletRequest request = request("/api/auth/login", "POST");

        auditLogger.logSecuritySuccess("LOGIN_SUCCESS", request, 1L, "ROLE_USER");

        ILoggingEvent event = appender.list.get(0);
        Map<String, String> mdc = event.getMDCPropertyMap();
        assertEquals("SECURITY_AUDIT", event.getFormattedMessage());
        assertEquals("SECURITY", mdc.get("eventCategory"));
        assertEquals("LOGIN_SUCCESS", mdc.get("eventName"));
        assertEquals("SUCCESS", mdc.get("eventOutcome"));
        assertEquals("1", mdc.get("userId"));
        assertEquals("ROLE_USER", mdc.get("userRole"));
        assertEquals("/api/auth/login", mdc.get("requestURI"));
        assertEquals("POST", mdc.get("method"));
        assertTrue(Boolean.TRUE.equals(request.getAttribute(AuditLogger.SECURITY_EVENT_LOGGED_ATTRIBUTE)));
    }

    @Test
    @DisplayName("운영 오류 로그는 중복 방지 속성과 오류 코드를 남긴다")
    void log_operational_error_marks_request_attribute() {
        MockHttpServletRequest request = request("/api/images/1", "GET");
        RuntimeException cause = new RuntimeException("s3 timeout");

        auditLogger.logOperationalError("AWS_S3_LOAD_ERROR", request, "IMAGE_LOAD_ERROR", 500, cause);

        ILoggingEvent event = appender.list.get(0);
        Map<String, String> mdc = event.getMDCPropertyMap();
        assertEquals("OPERATIONAL_ERROR", event.getFormattedMessage());
        assertEquals("OPERATIONAL", mdc.get("eventCategory"));
        assertEquals("AWS_S3_LOAD_ERROR", mdc.get("eventName"));
        assertEquals("FAILURE", mdc.get("eventOutcome"));
        assertEquals("IMAGE_LOAD_ERROR", mdc.get("errorName"));
        assertEquals("500", mdc.get("httpStatus"));
        assertTrue(Boolean.TRUE.equals(request.getAttribute(AuditLogger.OPERATIONAL_ERROR_LOGGED_ATTRIBUTE)));
    }

    @Test
    @DisplayName("도메인 쓰기 결과 로그는 소유자 유형, 응답 상태, 처리 시간을 MDC에 포함한다")
    void log_domain_write_result_writes_structured_mdc() {
        MockHttpServletRequest request = request("/api/brewery-priv/joy-add", "POST");

        auditLogger.logDomainWriteResult("BREWERY", request, 200, 15L);

        ILoggingEvent event = appender.list.get(0);
        Map<String, String> mdc = event.getMDCPropertyMap();
        assertEquals("DOMAIN_WRITE_RESULT", event.getFormattedMessage());
        assertEquals("DOMAIN_WRITE", mdc.get("eventCategory"));
        assertEquals("BREWERY_WRITE_RESULT", mdc.get("eventName"));
        assertEquals("SUCCESS", mdc.get("eventOutcome"));
        assertEquals("BREWERY", mdc.get("ownerType"));
        assertEquals("200", mdc.get("httpStatus"));
        assertEquals("15", mdc.get("durationMs"));
        assertTrue(Boolean.TRUE.equals(request.getAttribute(AuditLogger.DOMAIN_WRITE_LOGGED_ATTRIBUTE)));
    }

    private MockHttpServletRequest request(String uri, String method) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
