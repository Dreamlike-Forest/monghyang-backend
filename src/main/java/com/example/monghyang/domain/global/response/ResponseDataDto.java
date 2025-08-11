package com.example.monghyang.domain.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 json 직렬화하지 않게 설정
public class ResponseDataDto<T> {
    // 모든 데이터 조회 응답에 사용되는 dto

    private Integer status; // 정상적인 응답만 취급하므로, http status 200으로 고정
    private String message; // 성공 응답 메시지
    private T content; // 제네릭(T)은 객체 혹은 컬렉션 타입이 될 수 있습니다.

    private ResponseDataDto(String message, T content) {
        this.status = 200;
        this.message = message;
        this.content = content;
    }

    private ResponseDataDto(String message) {
        this.status = 200;
        this.message = message;
    }

    public static <T> ResponseDataDto<T> success(String message) { // 요청 처리 성공 응답
        return new ResponseDataDto<>(message);
    }

    public static <T> ResponseDataDto<T> messageContentOf(String message, T content) { // 조회 데이터 포함한 성공 응답
        return new ResponseDataDto<>(message, content);
    }
}
