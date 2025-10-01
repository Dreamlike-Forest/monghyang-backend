package com.example.monghyang.domain.global.order.payment.component;

import com.example.monghyang.domain.global.order.payment.status.AcquireStatus;
import com.example.monghyang.domain.global.order.payment.type.CardType;
import com.example.monghyang.domain.global.order.payment.type.InterestPayer;
import com.example.monghyang.domain.global.order.payment.type.OwnerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    private int amount;                 // 카드사에 결제 요청한 금액(즉시 할인 금액 포함)
    private String issuerCode;          // 카드 발급사 코드
    private String acquirerCode;        // 카드 매입사 코드
    private String number;              // 카드 번호(마스킹)
    private int installmentPlanMonths;  // 할부 개월 수 (0이면 일시불)
    private String approveNo;           // 카드사 승인 번호
    private boolean useCardPoint;       // 카드사 포인트 사용 여부
    private CardType cardType;            // 카드 종류 (신용, 체크, 기프트, 미확인)
    private OwnerType ownerType;           // 카드 소유자 타입 (개인, 법인, 미확인)
    private AcquireStatus acquireStatus;       // 카드 결제 매입 상태
    private boolean isInterestFree;     // 무이자 할부 여부
    private InterestPayer interestPayer;       // 할부 수수료 부담 주체(BUYER, CARD_COMPANY, MERCHANT)
}