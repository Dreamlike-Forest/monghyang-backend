package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cancel {
    private Integer cancelAmount;            // 취소된 금액
    private String cancelReason;         // 결제 취소 사유
    private Integer taxFreeAmount;           // 취소된 금액 중 면세 금액
    private Integer taxExemptionAmount;      // 취소된 금액 중 과세 제외 금액(컵 보증금 등)
    private Integer refundableAmount;        // 결제 취소 후 환불 가능한 잔액
    private Integer transferDiscountAmount;  // 퀵계좌이체 즉시할인에서 취소된 금액
    private Integer easyPayDiscountAmount;   // 간편결제 포인트/쿠폰 등에서 취소된 금액
    private String canceledAt;           // 결제 취소 시각 (ISO 8601)
    private String transactionKey;       // 취소 건의 거래 키값
    private String cancelStatus;         // 취소 상태(DONE: 성공적으로 취소된 상태)
    private String cancelRequestId;      // 취소 요청 ID(비동기 결제 등에서 사용)
    private String receiptKey;           // 취소 건의 현금영수증 키값
}
