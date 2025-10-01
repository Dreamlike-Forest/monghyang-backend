package com.example.monghyang.domain.global.order.payment.status;

public enum RefundStatus {
    NONE,  //환불 요청이 없는 상태입니다.
    PENDING, // 환불을 처리 중인 상태입니다.
    FAILED,  // 환불에 실패한 상태입니다.
    PARTIAL_FAILED, // 부분 환불에 실패한 상태입니다.
    COMPLETED // 환불이 완료된 상태입니다.
}
