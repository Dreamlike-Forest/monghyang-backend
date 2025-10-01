package com.example.monghyang.domain.global.pg;

import com.example.monghyang.domain.global.pg.component.BillingTransfer;
import com.example.monghyang.domain.global.pg.component.Card;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Billing { // 자동결제에 사용할 결제수단이 인증되어 등록됐을 때 돌아오는 객체
    private String mId; // 상점아이디. PG에서 발급
    private String customerKey; // 구매자 ID. BillingKey와 연결. UUID 사용 권장
    private String authenticatedAt; // 결제수단 인증된 시점의 날짜, 시간 정보
    private String method; // 결제 수단
    private String billingKey; // 자동결제에서 카드 정보 대신 사용되는 값. customerKey와 연결된다.
    private Card card; // 발급된 빌링키와 연결된 카드 정보
    private List<BillingTransfer> transfers; // 발급된 빌링키와 연동된 계좌 정보
    private String cardCompany; // 카드 발급사
    private String cardNumber; // 카드 번호(일부 마스킹)
}
