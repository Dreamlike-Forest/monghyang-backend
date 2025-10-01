package com.example.monghyang.domain.global.order.payment.component;

import com.example.monghyang.domain.global.order.payment.status.SettlementStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobilePhone {
    private String customerMobilePhone; // 결제에 사용한 휴대폰 번호(숫자만, 8~15자)
    private SettlementStatus settlementStatus;    // 정산 상태(INCOMPLETED, COMPLETED)
    private String receiptUrl;          // 휴대폰 결제 영수증 URL
}
