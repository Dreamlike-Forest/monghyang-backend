package com.example.monghyang.domain.security.filter;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@Order(2)
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            try {
                filterChain.doFilter(request, response);
            } catch (ApplicationException e) {
                setApplicationErrorResponse(request, response, e.getApplicationError());
            } catch (IllegalStateException e) {
                return;
            }
        } catch (IOException e) {
            log.error(e.getMessage()+"\n response http body 작성 도중 에러가 발생했습니다.");
            e.printStackTrace();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    // 에러 처리용 dto를 통해 http 응답의 body에 http status code, error message 값을 json으로 반환합니다.
    // 이 경우 로그 처리 및 프론트엔드 개발 편의성을 위해 body에도 status code를 첨부합니다.
    public void setApplicationErrorResponse(HttpServletRequest request, HttpServletResponse response, ApplicationError applicationError) throws IOException {
        response.setStatus(applicationError.getStatus().value());
        response.setContentType("application/json;charset=utf-8");
        // 필터 레벨의 json 직렬화를 위해 objectMapper 이용
        objectMapper.writeValue(response.getWriter(), ApplicationErrorDto.requestStatusMessageOf(request, applicationError.getStatus(), applicationError.getMessage()));
    }

}
