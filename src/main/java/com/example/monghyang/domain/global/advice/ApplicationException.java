package com.example.monghyang.domain.global.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
    private final ApplicationError applicationError;
    public ApplicationException(ApplicationError applicationError) {
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
