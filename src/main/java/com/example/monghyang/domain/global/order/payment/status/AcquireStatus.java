package com.example.monghyang.domain.global.order.payment.status;

public enum AcquireStatus {
    READY,      // 아직 매입 요청이 안 된 상태입니다.
    REQUESTED,  // 매입이 요청된 상태입니다.
    COMPLETED, // 요청된 매입이 완료된 상태입니다.
    CANCEL_REQUESTED, // 매입 취소가 요청된 상태입니다.
    CANCELED // 요청된 매입 취소가 완료된 상태입니다.
}
