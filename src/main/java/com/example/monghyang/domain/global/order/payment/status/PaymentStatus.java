package com.example.monghyang.domain.global.order.payment.status;

public enum PaymentStatus {
    READY,                  // 결제 초기 상태(인증 전)
    IN_PROGRESS,            // 인증 완료 상태
    WAITING_FOR_DEPOSIT,    // 가상계좌 결제 흐름에만 있는 상태. 발급된 가상계좌에 아직 입금되지 않은 상태
    DONE,                   // 인증된 결제수단으로 요청한 결제가 승인된 상태
    CANCELED,               // 승인된 결제가 취소된 상태
    PARTIAL_CANCELED,       // 승인된 결제가 부분 취소된 상태
    ABORTED,                // 결제 승인 실패 상태
    EXPIRED                 // 결제 유효 시간 30분이 지나 거래가 취소된 상태
}
