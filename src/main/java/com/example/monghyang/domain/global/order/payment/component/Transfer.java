package com.example.monghyang.domain.global.order.payment.component;

import com.example.monghyang.domain.global.order.payment.status.SettlementStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transfer {
    private String bankCode;            // 은행 두 자리 코드
    private SettlementStatus settlementStatus;    // 정산 상태(INCOMPLETED, COMPLETED)
}
