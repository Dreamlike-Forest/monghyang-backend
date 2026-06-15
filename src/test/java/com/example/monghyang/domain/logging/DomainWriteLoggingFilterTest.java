package com.example.monghyang.domain.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DomainWriteLoggingFilterTest {
    @Mock
    AuditLogger auditLogger;

    @Test
    @DisplayName("양조장 전용 쓰기 요청은 성공 결과를 도메인 감사 로그로 남긴다")
    void brewery_write_success_writes_domain_audit_log() throws Exception {
        DomainWriteLoggingFilter filter = new DomainWriteLoggingFilter(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/brewery-priv/joy-add");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);

        filter.doFilter(request, response, chain);

        verify(auditLogger).logDomainWriteResult(eq("BREWERY"), eq(request), eq(200), anyLong());
    }

    @Test
    @DisplayName("판매자 전용 쓰기 요청은 실패 결과도 도메인 감사 로그로 남긴다")
    void seller_write_failure_writes_domain_audit_log() throws Exception {
        DomainWriteLoggingFilter filter = new DomainWriteLoggingFilter(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/seller-priv/product/10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_FORBIDDEN);

        filter.doFilter(request, response, chain);

        verify(auditLogger).logDomainWriteResult(eq("SELLER"), eq(request), eq(403), anyLong());
    }

    @Test
    @DisplayName("양조장 전용 조회 요청은 도메인 쓰기 감사 로그를 남기지 않는다")
    void brewery_get_request_does_not_write_domain_audit_log() throws Exception {
        DomainWriteLoggingFilter filter = new DomainWriteLoggingFilter(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/brewery-priv/joy");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);

        filter.doFilter(request, response, chain);

        verify(auditLogger, never()).logDomainWriteResult(anyString(), any(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("일반 쓰기 요청은 도메인 쓰기 감사 로그를 남기지 않는다")
    void public_write_request_does_not_write_domain_audit_log() throws Exception {
        DomainWriteLoggingFilter filter = new DomainWriteLoggingFilter(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/community");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);

        filter.doFilter(request, response, chain);

        verify(auditLogger, never()).logDomainWriteResult(anyString(), any(), anyInt(), anyLong());
    }
}
