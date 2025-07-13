package com.example.monghyang.domain.global.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
    private ApplicationError applicationError;
    public ApplicationException(ApplicationError applicationError) {
        this.applicationError = applicationError;
    }

    public HttpStatus getHttpStatus() {
        return applicationError.getStatus();
    }

    public String getMessage() {
        return applicationError.getMessage();
    }

}
