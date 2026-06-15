package com.example.monghyang.domain.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingFilterTest {
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private LoggingFilter filter;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LoggingFilter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        filter = new LoggingFilter();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("정상 응답은 공통 응답 로그를 남기지 않는다")
    void successful_response_does_not_write_response_log() throws Exception {
        executeWithStatus(HttpServletResponse.SC_OK);

        assertEquals(0, appender.list.size());
    }

    @Test
    @DisplayName("예상 가능한 4xx 응답은 공통 응답 로그를 남기지 않는다")
    void expected_client_error_does_not_write_response_log() throws Exception {
        executeWithStatus(HttpServletResponse.SC_NOT_FOUND);

        assertEquals(0, appender.list.size());
    }

    @Test
    @DisplayName("운영 오류로 기록되지 않은 5xx 응답은 안전망 로그를 남긴다")
    void unlogged_server_error_writes_fallback_log() throws Exception {
        executeWithStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        assertEquals(1, appender.list.size());
        assertEquals("RES_ERROR", appender.list.get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("이미 운영 오류로 기록된 5xx 응답은 중복 응답 로그를 남기지 않는다")
    void logged_server_error_does_not_write_duplicate_response_log() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setAttribute(AuditLogger.OPERATIONAL_ERROR_LOGGED_ATTRIBUTE, Boolean.TRUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        filter.doFilter(request, response, chain);

        assertEquals(0, appender.list.size());
    }

    private void executeWithStatus(int status) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(status);

        filter.doFilter(request, response, chain);
    }
}
