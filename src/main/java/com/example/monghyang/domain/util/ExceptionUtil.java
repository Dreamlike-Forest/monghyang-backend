package com.example.monghyang.domain.util;

import com.example.monghyang.domain.util.dto.RequestPathDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
public class ExceptionUtil {
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
