package com.example.monghyang.domain.global.pg;

import com.example.monghyang.domain.global.pg.component.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Settlement {
    private String mId;                 // 상점아이디(MID)
    private String paymentKey;          // 결제의 키값
    private String transactionKey;      // 거래의 키값
    private String orderId;             // 주문번호
    private String currency;            // 결제 통화
    private String method;              // 결제수단(카드, 가상계좌 등)
    private int amount;                 // 결제 금액
    private int interestFee;            // 할부 수수료 금액
    private List<Fee> fees;             // 결제 수수료 상세 정보(배열)
    private int supplyAmount;           // 결제 수수료 공급가액
    private int vat;                    // 결제 수수료 부가세
    private int payOutAmount;           // 지급 금액(수수료 제외)
    private String approvedAt;          // 거래 승인 시각(ISO 8601)
    private String soldDate;            // 정산 매출일(yyyy-MM-dd)
    private String paidOutDate;         // 정산 지급일(yyyy-MM-dd)
    private Card card;                  // 카드 결제 정보(선택적)
    private EasyPay easyPay;            // 간편결제 정보(선택적)
    private GiftCertificate giftCertificate; // 상품권 결제 정보(선택적)
    private MobilePhone mobilePhone;    // 휴대폰 결제 정보(선택적)
    private Transfer transfer;          // 계좌이체 정보(선택적)
    private VirtualAccount virtualAccount; // 가상계좌 정보(선택적)
    private Cancel cancel;              // 결제 취소 이력(선택적)
}
