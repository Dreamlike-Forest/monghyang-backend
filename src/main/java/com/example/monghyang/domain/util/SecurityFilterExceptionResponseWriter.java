package com.example.monghyang.domain.util;

import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityFilterExceptionResponseWriter {
    private final ObjectMapper objectMapper;

    /**
     * 에러 처리용 dto를 통해 http 응답의 body에 http status code, error message 값을 json으로 반환합니다.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param e ApplicationException
     */
    public void setApplicationErrorResponse(HttpServletRequest request, HttpServletResponse response, ApplicationException e) {
        MDC.put("level", e.getLogLevel().toString());
        MDC.put("message", e.getMessage());
        if(e.getLogStackTrace() == true) {
            log.warn("stackTrace", e);
        }
        response.setStatus(e.getHttpStatus().value());
        response.setContentType("application/json;charset=utf-8");
        // 필터 레벨의 json 직렬화를 위해 objectMapper 이용
        try {
            objectMapper.writeValue(response.getWriter(), ApplicationErrorDto.requestStatusMessageOf(request, e.getHttpStatus(), e.getMessage()));
        } catch (IOException ex) {
            // 응답 객체 작성 실패 시 로깅
            log.error(ex.getMessage(), ex);
        }
    }
}
