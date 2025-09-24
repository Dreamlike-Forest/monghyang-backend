package com.example.monghyang.domain.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.Collections;
import java.util.List;

@Configuration
public class SessionConfig {
    // 요청의 SID 추출 및 응답의 SID 헤더 세팅
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new HttpSessionIdResolver() {
            private static final String HEADER_NAME = "X-Session-Id";
            @Override // session id 추출
            public List<String> resolveSessionIds(HttpServletRequest request) {
                String sessionId = request.getHeader(HEADER_NAME);
                // 헤더에 여러 개의 세션 아이디가 포함되어 있어도 첫번째 요소만 취급
                return sessionId != null ? Collections.singletonList(sessionId) : Collections.emptyList();
            }

            @Override // set session
            public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
                response.setHeader(HEADER_NAME, sessionId);
            }

            @Override // delete session
            public void expireSession(HttpServletRequest request, HttpServletResponse response) {
                response.setHeader(HEADER_NAME, "");
            }
        };
    }
}
