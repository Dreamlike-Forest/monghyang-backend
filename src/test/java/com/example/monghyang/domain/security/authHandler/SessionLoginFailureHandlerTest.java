package com.example.monghyang.domain.security.authHandler;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.logging.AuditLogger;
import com.example.monghyang.domain.util.SecurityFilterExceptionResponseWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionLoginFailureHandlerTest {
    @Mock
    SecurityFilterExceptionResponseWriter writer;
    @Mock
    AuditLogger auditLogger;

    @Test
    @DisplayName("폼 로그인 실패는 실패 감사 로그를 남기고 오류 응답을 작성한다")
    void login_failure_writes_audit_log() throws Exception {
        SessionLoginFailureHandler handler = new SessionLoginFailureHandler(writer, auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));

        verify(auditLogger).logSecurityFailure("LOGIN_FAILURE", request, null, null, ApplicationError.USER_UNAUTHORIZED);
        verify(writer).setApplicationErrorResponse(eq(request), eq(response), any());
    }
}
