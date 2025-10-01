package com.example.monghyang.domain.global.pg.status;

public enum PGSellerStatus {
    APPROVAL_REQUIRED, // 지급대행 불가 상태. 개인 및 개인사업자 셀러 등록 직후의 상태이며, 본인인증이 필요
    PARTIALLY_APPROVED,  // 일주일 동안 1천만원까지 지급대행 가능한 상태
    KYC_REQUIRED, // 지급대행 불가능한 상태. 일주일 동안 1천만원을 초과하는 금액을 지급 요청하면 셀러는 해당 상태로 변경. 셀러가 KYC 인증 필요
    APPROVED // 금액 제한 없이 지급대행이 가능한 상태
}
