package com.example.monghyang.domain.global.pg.payment.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Failure {
    private String code; // 오류 타입 코드
    private String message; // 에러 메시지(에러 발생 이유)
}
