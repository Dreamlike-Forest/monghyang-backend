package com.example.monghyang.domain.global.pg.component;

import com.example.monghyang.domain.global.pg.status.SettlementStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GiftCertificate {
    private String approveNo;           // 결제 승인번호(최대 8자)
    private SettlementStatus settlementStatus;    // 정산 상태(INCOMPLETED, COMPLETED)
}
