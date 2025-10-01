package com.example.monghyang.domain.global.pg.payment.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EasyPay {
    private String provider;            // 간편결제사 코드
    private int amount;                 // 간편결제 계좌/포인트 결제 금액
    private int discountAmount;         // 즉시 할인된 금액(포인트/쿠폰 등)
}
