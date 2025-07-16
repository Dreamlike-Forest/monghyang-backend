package com.example.monghyang.domain.global.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseDto<T> {
    // 모든 정상적인 응답에 사용되는 dto

    private int status = 200; // 정상적인 응답만 취급하므로, http status 200으로 고정
    private T content; // 제네릭(T)은 객체 혹은 컬렉션 타입이 될 수 있습니다.

    private ResponseDto(T content) {
        this.content = content;
    }

    public static <T> ResponseDto<T> contentFrom(T content) {
        return new ResponseDto<T>(content);
    }
}
