package com.example.monghyang.domain.global.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApplicationError {
    // 회원 및 인증/인가
    USER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "아이디와 비밀번호를 정확히 입력해 주세요."),
    REQUEST_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    AUTH_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "요청의 헤더에 세션 아이디가 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "요청의 헤더에 refresh token이 존재하지 않습니다."),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "아이디와 비밀번호가 일치하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다. 다시 로그인 해주세요."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "세션 정보를 찾을 수 없습니다. 갱신 요청을 보내주세요."),
    TOKEN_IMPAIRED(HttpStatus.UNAUTHORIZED, "토큰이 훼손되었습니다."),
    CONCURRENT_CONNECTION(HttpStatus.CONFLICT, "다른 곳에서 새로 접속하여 로그아웃합니다."),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 Role 입니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 Region 입니다."),
    TERMS_AND_CONDITIONS_NOT_AGREED(HttpStatus.CONFLICT, "약관에 동의하지 않으면 회원 가입하실 수 없습니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    SESSION_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "세션 생성 중 에러가 발생했습니다.");


    private final HttpStatus status;
    private final String message;
    ApplicationError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
