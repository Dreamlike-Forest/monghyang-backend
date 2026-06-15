package com.example.monghyang.domain.oauth2.handler;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.logging.AuditLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final AuditLogger auditLogger;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        auditLogger.logSecurityFailure("OAUTH2_LOGIN_FAILURE", request, null, null, ApplicationError.USER_UNAUTHORIZED);
        String errorMsg = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        response.setContentType("/login?error="+errorMsg);
    }
}
