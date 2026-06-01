package com.example.monghyang.domain.global.advice;

import com.example.monghyang.domain.util.dto.RequestPathDto;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "공통 실패 응답 형식")
public class ApplicationErrorDto {
    @Schema(description = "HTTP 상태 코드입니다.", example = "400")
    private int status;
    @Schema(description = "요청 HTTP 메서드입니다.", example = "POST")
    private String method;
    @Schema(description = "요청 경로입니다.", example = "/api/brewery-priv/schedule")
    private String path;
    @Schema(description = "클라이언트에 전달되는 실패 메시지입니다.", example = "잘못된 시간/날짜 정보입니다.")
    private String message;
    @Schema(description = "서버가 실패 응답을 생성한 시각입니다.", example = "2026-06-01T12:00:00")
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
