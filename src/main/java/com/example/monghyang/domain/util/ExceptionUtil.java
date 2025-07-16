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

    // Filter 발생 예외 처리 메소드
    // 에러 처리용 dto를 통해 http 응답의 body에 http status code, error message 값을 json으로 반환합니다.
    // 이 경우 로그 처리 및 프론트엔드 개발 편의성을 위해 body에도 status code를 첨부합니다.
    public static void filterExceptionHandler(HttpServletResponse response, ApplicationError applicationError) {
        response.setStatus(applicationError.getStatus().value());
        response.setContentType("application/json;charset=utf-8");
        try {
            // 필터 레벨의 json 직렬화를 위해 objectMapper 이용
            objectMapper.writeValue(response.getWriter(), ApplicationErrorDto.statusMessageOf(applicationError.getStatus(), applicationError.getMessage()));
        } catch (IOException e) {
            log.error(e.getMessage()+"\n response http body 작성 도중 에러가 발생했습니다.");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
