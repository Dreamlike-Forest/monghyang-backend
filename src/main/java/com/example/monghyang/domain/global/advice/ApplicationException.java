package com.example.monghyang.domain.global.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
    private final ApplicationError applicationError;
    public ApplicationException(ApplicationError applicationError) {
        this.applicationError = applicationError;
    }

    /**
     * 원본 예외의 cause 정보를 포함하여 ApplicationException 생성
     * @param applicationError 클라이언트에 응답하려는 ApplicationError
     * @param cause 원본 예외의 cause
     */
    public ApplicationException(ApplicationError applicationError, Throwable cause) {
        super(applicationError.getMessage(), cause); // applicationError 메시지와 원본 예외 cause를 세팅
        this.applicationError = applicationError;
    }

    public HttpStatus getHttpStatus() {
        return applicationError.getStatus();
    }

    public String getMessage() {
        return applicationError.getMessage();
    }
    public ErrorType getErrorType() {
        return applicationError.getErrorType();
    }
    public LogLevel getLogLevel() {
        return applicationError.getLogLevel();
    }
    public boolean getLogStackTrace() {
        return applicationError.isLogStackTrace();
    }

}
