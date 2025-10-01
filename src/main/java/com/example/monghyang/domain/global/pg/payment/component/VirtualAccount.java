package com.example.monghyang.domain.global.pg.payment.component;

import com.example.monghyang.domain.global.pg.payment.status.RefundStatus;
import com.example.monghyang.domain.global.pg.payment.type.AccountType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VirtualAccount {
    private AccountType accountType;         // 가상계좌 타입 (일반, 고정)
    private String accountNumber;       // 발급된 계좌번호
    private String bankCode;            // 은행 코드
    private String customerName;        // 가상계좌 발급 구매자명
    private String depositorName;       // 입금자명
    private String dueDate;             // 입금 기한 (ISO 8601)
    private RefundStatus refundStatus;        // 환불 처리 상태
    private boolean expired;            // 가상계좌 만료 여부
    private String settlementStatus;    // 정산 상태
    private RefundReceiveAccount refundReceiveAccount; // 환불 계좌 정보
}
