package com.example.monghyang.domain.global.pg;

import com.example.monghyang.domain.global.pg.status.PGPaymentStatus;
import com.example.monghyang.domain.global.pg.type.MethodType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction { // 거래 정보를 담고 있는 객체
    private String mId; // 상점아이디
    private String transactionKey; // 거래의 키값. 한 건의 결제 건의 승인 거래와 취소 거래를 구분하는 데 사용
    private String paymentKey; // 결제의 키값
    private String orderId; // 주문번호
    private MethodType method; // 결제 수단
    private String customerKey; // 구매자 ID. BillingKey와 연결된다. UUID로 사용
    private Boolean useEscrow; // 에스크로 사용 여부
    private String receiptUrl; // 거래 영수증 확인할 수 있는 주소
    private PGPaymentStatus status; // 결제 처리 상태
    private String transactionAt; // 결제 처리 시점의 날짜와 시간 정보
    private String currency; // 결제 시 사용한 통화
    private Integer amount; // 결제한 금액
}
