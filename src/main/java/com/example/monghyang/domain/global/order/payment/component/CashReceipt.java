package com.example.monghyang.domain.global.order.payment.component;

import com.example.monghyang.domain.global.order.payment.type.CashReceiptType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashReceipt {
    private CashReceiptType type;                    // 현금영수증 종류(소득공제, 지출증빙)
    private String receiptKey;              // 현금영수증 키값(최대 200자)
    private String issueNumber;             // 현금영수증 발급번호(최대 9자)
    private String receiptUrl;              // 현금영수증 확인용 URL
    private int amount;                     // 현금영수증 처리 금액
    private int taxFreeAmount;              // 면세 처리 금액
}
