package com.example.monghyang.domain.global.pg.status;

public enum PayoutStatus {
    REQUESTED,  // 지급 요청되었지만 아직 처리되지 않은 상태
    IN_PROGRESS, // 지급을 처리하고 있는 상태
    COMPLETED,  // 셀러에 지급이 완료된 상태
    FAILED, //  지급 요청이 실패한 상태
    CANCELED    // 지급 요청이 취소된 상태
}
