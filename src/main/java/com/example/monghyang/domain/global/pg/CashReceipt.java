package com.example.monghyang.domain.global.pg;

import com.example.monghyang.domain.global.pg.component.Failure;
import com.example.monghyang.domain.global.pg.status.IssueStatus;
import com.example.monghyang.domain.global.pg.type.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashReceipt {
    // 현금영수증의 키값 (최대 200자)
    private String receiptKey;

    // 현금영수증 발급번호 (최대 9자)
    private String issueNumber;

    // 현금영수증 발급 상태 (IN_PROGRESS, COMPLETED, FAILED)
    private IssueStatus issueStatus;

    // 현금영수증 처리된 금액
    private Integer amount;

    // 면세 처리된 금액
    private Integer taxFreeAmount;

    // 주문번호 (6자 이상 64자 이하)
    private String orderId;

    // 구매상품명 (최대 100자)
    private String orderName;

    // 현금영수증의 종류 (소득공제, 지출증빙)
    private String type;

    // 현금영수증 발급 종류 (CONFIRM: 발급, CANCEL: 취소)
    private TransactionType transactionType;

    // 현금영수증을 발급한 사업자등록번호 (10자)
    private String businessNumber;

    // 현금영수증 발급에 필요한 소비자 인증수단 (최대 30자)
    private String customerIdentityNumber;

    // 결제 실패 정보 (에러 코드 및 메시지)
    private Failure failure;

    // 현금영수증 발급 혹은 취소 요청 날짜와 시간 (ISO 8601 형식)
    private String requestedAt;

    // 발행된 현금영수증을 확인할 수 있는 URL
    private String receiptUrl;
}
