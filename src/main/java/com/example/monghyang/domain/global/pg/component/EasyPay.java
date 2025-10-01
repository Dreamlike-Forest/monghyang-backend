package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EasyPay {
    private String provider;            // 간편결제사 코드
    private Integer amount;                 // 간편결제 계좌/포인트 결제 금액
    private Integer discountAmount;         // 즉시 할인된 금액(포인트/쿠폰 등)
}
