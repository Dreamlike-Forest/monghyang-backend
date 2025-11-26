package com.example.monghyang.domain.global.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApplicationError {
    // 회원 및 인증/인가
    USER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "아이디와 비밀번호를 정확히 입력해 주세요.", ErrorType.SECURITY, LogLevel.WARN, true),
    REQUEST_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", ErrorType.SECURITY, LogLevel.ERROR, true),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    BREWERY_NOT_FOUND(HttpStatus.NOT_FOUND, "양조장 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    BREWERY_OPENING_TIME_INVALID(HttpStatus.BAD_REQUEST, "양조장 운영 시간대가 잘못되었습니다.", ErrorType.BUSINESS, LogLevel.WARN, true),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    PRODUCT_CANNOT_ORDER(HttpStatus.BAD_REQUEST, "주문할 수 없는 상품입니다.", ErrorType.BUSINESS, LogLevel.WARN, true),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 장바구니 요소가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니가 비어있습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    JOY_NOT_FOUND(HttpStatus.NOT_FOUND, "체험 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    JOY_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "체험 예약 내역 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    JOY_ORDER_TIME_UPDATE_ERROR(HttpStatus.BAD_REQUEST, "체험 날짜 하루 전날까지만 변경할 수 있습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    JOY_ORDER_CANCEL_ERROR(HttpStatus.BAD_REQUEST, "체험 날짜 하루 전날까지만 취소할 수 있습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    JOY_ORDER_DELETE_ERROR(HttpStatus.BAD_REQUEST, "체험 종료 이후부터 예약 내역을 삭제할 수 있습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    JOY_ORDER_TIME_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 체험 시간대입니다.", ErrorType.BUSINESS, LogLevel.WARN, true),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "배송 시작 전인 상품만 취소할 수 있습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "상태 변경 내역이 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 요소 정보가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    JOY_TIME_DUPLICATE(HttpStatus.CONFLICT, "이미 해당 시간에 다른 예약이 존재합니다. 다른 시간대를 선택해주세요.", ErrorType.BUSINESS, LogLevel.INFO, false),
    JOY_COUNT_OVER(HttpStatus.BAD_REQUEST, "예약 가능 인원수를 초과하였습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    AUTH_INFO_NOT_FOUND(HttpStatus.UNAUTHORIZED, "세션(인증) 정보가 존재하지 않습니다.", ErrorType.SECURITY, LogLevel.WARN, true),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "요청의 헤더에 refresh token이 존재하지 않습니다.", ErrorType.SECURITY, LogLevel.WARN, true),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "아이디와 비밀번호가 일치하지 않습니다.", ErrorType.SECURITY, LogLevel.WARN, true),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다. 다시 로그인 해주세요.", ErrorType.SECURITY, LogLevel.DEBUG, true),
    SESSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "세션 정보가 존재하지 않습니다.", ErrorType.SECURITY, LogLevel.ERROR, true),
    IMAGE_REQUEST_NULL(HttpStatus.BAD_REQUEST, "업로드할 이미지 파일이 null 입니다.", ErrorType.BUSINESS, LogLevel.WARN, true),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    IMAGE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 서버에서 에러가 발생했습니다.", ErrorType.SYSTEM, LogLevel.ERROR, true),
    IMAGE_REMOVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 제거 중 서버에서 에러가 발생했습니다.", ErrorType.SYSTEM, LogLevel.ERROR, true),
    IMAGE_LOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 로드 중 서버에서 에러가 발생했습니다.", ErrorType.SYSTEM, LogLevel.ERROR, true),
    IMAGE_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "이미지 형식이 잘못되었습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, true),
    IMAGE_SIZE_OVER(HttpStatus.BAD_REQUEST, "10MB가 넘는 이미지를 업로드할 수 없습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, true),
    IMAGE_SIZE_ERROR(HttpStatus.BAD_REQUEST, "잘못된 크기의 이미지입니다.", ErrorType.BUSINESS, LogLevel.DEBUG, true),
    IMAGE_SEQ_NULL(HttpStatus.BAD_REQUEST, "이미지의 순서 정보(seq)가 누락되었습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, true),
    IMAGE_SEQ_INVALID(HttpStatus.BAD_REQUEST, "이미지의 순서 정보가 올바르지 않습니다. 수정사항을 롤백합니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    IMAGE_COUNT_INVALID(HttpStatus.BAD_REQUEST, "이미지는 최대 5개 업로드할 수 있습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, true),
    TOKEN_IMPAIRED(HttpStatus.UNAUTHORIZED, "토큰이 훼손되었습니다.", ErrorType.SECURITY, LogLevel.ERROR, true),
    CONCURRENT_CONNECTION(HttpStatus.CONFLICT, "다른 곳에서 새로 접속하여 로그아웃합니다.", ErrorType.BUSINESS, LogLevel.WARN, true),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 Role 입니다.", ErrorType.BUSINESS, LogLevel.WARN, true),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 Region 입니다.", ErrorType.BUSINESS, LogLevel.WARN, true),
    TAG_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "태그 카테고리가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "태그가 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    COMMUNITY_NOT_FOUND(HttpStatus.NOT_FOUND, "커뮤니티 게시글이 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false), // 추가
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),   // 추가
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 누른 게시글입니다.", ErrorType.BUSINESS, LogLevel.INFO, false),  // 추가
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요를 누르지 않은 게시글입니다.", ErrorType.BUSINESS, LogLevel.INFO, false), // 추가
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다.", ErrorType.SECURITY, LogLevel.ERROR, true),    // 추가
    TERMS_AND_CONDITIONS_NOT_AGREED(HttpStatus.CONFLICT, "약관에 동의하지 않으면 회원 가입하실 수 없습니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.", ErrorType.BUSINESS, LogLevel.INFO, false),
    DTO_NULL_ERROR(HttpStatus.BAD_REQUEST, "DTO가 null입니다.", ErrorType.BUSINESS, LogLevel.DEBUG, false),
    NEW_PASSWORD_NULL(HttpStatus.BAD_REQUEST, "새 비밀번호를 입력해주세요.", ErrorType.BUSINESS, LogLevel.INFO, false),
    NOT_MATCH_CUR_PASSWORD(HttpStatus.UNAUTHORIZED, "기존 비밀번호가 일치하지 않습니다.", ErrorType.SECURITY, LogLevel.WARN, true),
    SESSION_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "세션 파싱 중 에러가 발생했습니다.", ErrorType.SYSTEM, LogLevel.ERROR, true),
    SESSION_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "세션 생성 중 에러가 발생했습니다.", ErrorType.SYSTEM, LogLevel.ERROR, true),
    MANIPULATE_ORDER_TOTAL_PRICE(HttpStatus.BAD_REQUEST, "주문 금액이 조작된 요청입니다.", ErrorType.SECURITY, LogLevel.ERROR, true),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 알 수 없는 에러가 발생했습니다.", ErrorType.SYSTEM, LogLevel.ERROR, true);


    private final HttpStatus status;
    private final String message;
    private final ErrorType errorType;
    private final LogLevel logLevel;
    private final boolean logStackTrace;
    ApplicationError(HttpStatus status, String message, ErrorType errorType, LogLevel logLevel, boolean logStackTrace) {
        this.status = status;
        this.message = message;
        this.errorType = errorType;
        this.logLevel = logLevel;
        this.logStackTrace = logStackTrace;
    }
}
