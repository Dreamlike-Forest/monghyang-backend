package com.example.monghyang.domain.global.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApplicationError {
    // 회원 및 인증/인가
    USER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "아이디와 비밀번호를 정확히 입력해 주세요."),
    REQUEST_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보가 존재하지 않습니다."),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자 정보가 존재하지 않습니다."),
    BREWERY_NOT_FOUND(HttpStatus.NOT_FOUND, "양조장 정보가 존재하지 않습니다."),
    AUTH_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "세션(인증) 정보가 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "요청의 헤더에 refresh token이 존재하지 않습니다."),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "아이디와 비밀번호가 일치하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다. 다시 로그인 해주세요."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "세션 정보가 존재하지 않습니다."),
    IMAGE_REQUEST_NULL(HttpStatus.BAD_REQUEST, "업로드할 이미지 파일이 null 입니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지가 존재하지 않습니다."),
    IMAGE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 서버에서 에러가 발생했습니다."),
    IMAGE_LOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 로드 중 서버에서 에러가 발생했습니다."),
    IMAGE_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "이미지 형식이 잘못되었습니다."),
    IMAGE_SIZE_OVER(HttpStatus.BAD_REQUEST, "10MB가 넘는 이미지를 업로드할 수 없습니다."),
    IMAGE_SIZE_ERROR(HttpStatus.BAD_REQUEST, "잘못된 크기의 이미지입니다."),
    IMAGE_SEQ_NULL(HttpStatus.BAD_REQUEST, "이미지의 순서 정보(seq)가 누락되었습니다."),
    IMAGE_SEQ_INVALID(HttpStatus.BAD_REQUEST, "이미지의 순서 정보가 올바르지 않습니다. 수정사항을 롤백합니다."),
    TOKEN_IMPAIRED(HttpStatus.UNAUTHORIZED, "토큰이 훼손되었습니다."),
    CONCURRENT_CONNECTION(HttpStatus.CONFLICT, "다른 곳에서 새로 접속하여 로그아웃합니다."),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 Role 입니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 Region 입니다."),
    TERMS_AND_CONDITIONS_NOT_AGREED(HttpStatus.CONFLICT, "약관에 동의하지 않으면 회원 가입하실 수 없습니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DTO_NULL_ERROR(HttpStatus.BAD_REQUEST, "DTO가 null입니다."),
    NEW_PASSWORD_NULL(HttpStatus.NOT_FOUND, "새 비밀번호를 입력해주세요."),
    NOT_MATCH_CUR_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호가 일치하지 않습니다."),
    SESSION_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "세션 파싱 중 에러가 발생했습니다."),
    SESSION_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "세션 생성 중 에러가 발생했습니다.");


    private final HttpStatus status;
    private final String message;
    ApplicationError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
