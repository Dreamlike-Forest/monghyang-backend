package com.example.monghyang.domain.global.order.payment.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundReceiveAccount {
    private String bankCode;            // 환불 계좌 은행 코드
    private String accountNumber;       // 환불 계좌번호
    private String holderName;          // 예금주명
}
