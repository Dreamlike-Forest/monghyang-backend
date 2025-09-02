package com.example.monghyang.domain.security.authHandler;

import com.example.monghyang.domain.security.filter.LogoutDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SessionLogoutSeccessHandler implements LogoutSuccessHandler {
    private final ObjectMapper objectMapper;
    @Autowired
    public SessionLogoutSeccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 기본적으로 Logout 시 클라이언트로 전달되는 리다이렉션 메시지 비활성화를 위한 핸들러 재정의
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), LogoutDto.successContentFrom());
    }
}
