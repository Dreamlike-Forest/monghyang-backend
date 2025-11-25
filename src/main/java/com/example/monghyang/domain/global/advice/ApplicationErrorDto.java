package com.example.monghyang.domain.global.advice;

import com.example.monghyang.domain.util.dto.RequestPathDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationErrorDto {
    private int status;
    private String method;
    private String path;
    private String message;
    private LocalDateTime timestamp;

    private ApplicationErrorDto(HttpServletRequest request, HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = request.getRequestURI();
        this.method = request.getMethod();
    }

    /**
     *
     * @param request 요청의 HttpServletRequest 객체
     * @param status 응답 헤더 및 본문 status 필드에 삽입할 http status 값
     * @param message 응답 본문 message
     * @return
     */
    public static ApplicationErrorDto requestStatusMessageOf(HttpServletRequest request, HttpStatus status, String message) {
        return new ApplicationErrorDto(request, status, message);
    }
}
