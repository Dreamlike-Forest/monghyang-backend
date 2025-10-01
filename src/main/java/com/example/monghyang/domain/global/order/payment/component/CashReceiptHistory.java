package com.example.monghyang.domain.global.order.payment.component;

import com.example.monghyang.domain.global.order.payment.status.IssueStatus;
import com.example.monghyang.domain.global.order.payment.type.CashReceiptType;
import com.example.monghyang.domain.global.order.payment.type.TransactionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashReceiptHistory {
    private String receiptKey;              // 현금영수증 키값(최대 200자)
    private String orderId;                 // 주문번호(6~64자, 영문/숫자/-, _)
    private String orderName;               // 구매상품명(최대 100자)
    private CashReceiptType type;           // 현금영수증 종류(소득공제, 지출증빙)
    private String issueNumber;             // 현금영수증 발급번호(최대 9자)
    private String receiptUrl;              // 현금영수증 확인용 URL
    private String businessNumber;          // 사업자등록번호(10자)
    private TransactionType transactionType;  // 발급(CONFIRM), 취소(CANCEL)
    private int amount;                     // 처리 금액
    private int taxFreeAmount;              // 면세 처리 금액
    private IssueStatus issueStatus;        // 발급 상태(IN_PROGRESS, COMPLETED, FAILED)
    private Failure failure;                // 결제 실패 객체
    private String customerIdentityNumber;  // 소비자 인증수단(최대 30자)
    private String requestedAt;             // 발급/취소 요청 시각(ISO 8601)
}
