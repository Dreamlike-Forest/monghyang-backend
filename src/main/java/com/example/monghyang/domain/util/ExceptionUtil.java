package com.example.monghyang.domain.util;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.util.dto.RequestPathDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@Slf4j
@Component
public class ExceptionUtil {

    private static ObjectMapper objectMapper; // 정적 메소드에서 사용되는 값이므로 필드 또한 정적 설정

    @Autowired
    public ExceptionUtil(ObjectMapper objectMapper) {
        ExceptionUtil.objectMapper = objectMapper;
    }

    // 현재 API 요청의 PATH를 구하는 메소드
    public static RequestPathDto getRequestPath() {
        // 현재 요청 정보 load
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes != null ? requestAttributes.getRequest() : null;
        if(request == null) {
            log.error("Doesn't exist request info");
            return null;
        }

        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String path = (queryString == null || queryString.isEmpty()) ? uri : uri + "?" + queryString; // 클라이언트 반환용 경로
        String fullPath = httpMethod + " " + path; // 로그 저장용 경로

        return RequestPathDto.httpMethodPathOf(httpMethod, path);
    }
}
