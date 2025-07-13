package com.example.monghyang.domain.global.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApplicationError {
    // 회원 및 인증/인가
    USER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "아이디와 비밀번호를 정확히 입력해 주세요."),
    REQUEST_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "아이디와 비밀번호가 일치하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "access token이 만료되었습니다"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 Role 입니다.");


    private final HttpStatus status;
    private final String message;
    ApplicationError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
