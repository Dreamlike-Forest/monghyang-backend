package com.example.monghyang.domain.global.order.payment;

import com.example.monghyang.domain.global.order.payment.component.*;
import com.example.monghyang.domain.global.order.payment.type.PaymentMethod;
import com.example.monghyang.domain.global.order.payment.type.PaymentType;
import com.example.monghyang.domain.orders.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 네트워크 전송에서 제외
public class Payment {
    private String version; // 객체 응답 버전 (날짜 기반 버저닝)
    private String paymentKey; // 결제의 키값
    private PaymentType type; // 결제 타입 정보
    private String orderId; // 주문번호
    private String orderName; // 구매상품
    private String mId; // 상점아이디(토스페이먼츠에서 발급)
    private String currency; // 결제 시 사용한 통화
    private PaymentMethod method; // 결제 수단
    private int totalAmount; // 총 결제 금액
    private int balanceAmount; // 취소할 수 있는 금액(잔고)
    private PaymentStatus status; // 결제 처리 상태
    private String requestedAt; // 결제 발생 날짜와 시간 정보
    private String approvedAt; // 결제 승인이 일어난 날짜와 시간 정보
    private boolean useEscrow; // 에스크로 사용 여부
    private String lastTransactionKey; // 마지막 거래(Transaction)의 키값
    private String suppliedAmount; // 공급가액
    private int vat; // 부가세. (결제 금액 - 면세 금액) / 11 후 소수점 첫째 자리에서 반올림해서 계산
    private boolean cultureExpense; // 문화비(도서, 공연 티켓, 박물관 혹은 미술관 입장 등) 지출 여부
    private int taxFreeAmount; // 면세 금액. 일반 상점인 경우 이 값이 0이다.
    private int taxExemptionAmount; // 과제 제외한 결제 금액
    private Cancel cancels; // 결제 취소 이력
    private boolean isPartialCancelable; // 부분 취소 가능 여부: 이 값이 false면 전액 환불만 가능
    private Card card; // 카드 결제 시 제공되는 정보
    private VirtualAccount virtualAccount; // 가상계좌 결제 시 제공되는 정보
    private String secret; // 웹훅을 검증하는 최대 50자 값
    private MobilePhone mobilePhone; // 휴대폰 결제 관련 정보
    private GiftCertificate giftCertificate; // 상품권 결제 관련 정보
    private Transfer transfer; // 계좌이체 결제 관련 정보
    private Metadata metadata; // 결제 요청 시 sdk에서 직접 추가할 수 있는 결제 관련 정보(최대 5개의 키-값 쌍)
    private Receipt receipt; // 발행된 영수증 정보
    private Checkout checkout; // 결제창 정보
    private EasyPay easyPay; // 간편결제 정보
    private String country; // 결제한 국가
    private Failure failure; // 결제 승인 실패 시 응답받는 에러 객체
    private CashReceipt cashReceipt; // 현금영수증 정보
    private List<CashReceiptHistory> cashReceipts; // 현금영수증 발행 및 취소 이력이 담기는 배열
    private Discount discount; // 카드사 및 퀵계좌이체의 즉시 할인 프로모션 정보
}
