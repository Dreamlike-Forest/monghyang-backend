package com.example.monghyang.domain.security.authHandler;

import com.example.monghyang.domain.auth.details.LoginUserDetails;
import com.example.monghyang.domain.logging.AuditLogger;
import com.example.monghyang.domain.util.SessionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionLoginSuccessHandlerTest {
    @Mock
    SessionUtil sessionUtil;
    @Mock
    AuditLogger auditLogger;

    @Test
    @DisplayName("폼 로그인 성공은 세션 생성 후 성공 감사 로그를 남긴다")
    void login_success_writes_audit_log_after_session_created() throws Exception {
        SessionLoginSuccessHandler handler = new SessionLoginSuccessHandler(new ObjectMapper(), sessionUtil, auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        LoginUserDetails principal = LoginUserDetails.builder()
                .userId(1L)
                .roleType("ROLE_USER")
                .nickname("사용자")
                .password("password")
                .build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(sessionUtil).createNewAuthInfo(request, response, 1L, "ROLE_USER");
        verify(auditLogger).logSecuritySuccess("LOGIN_SUCCESS", request, 1L, "ROLE_USER");
    }
}
