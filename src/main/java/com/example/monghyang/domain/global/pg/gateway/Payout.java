package com.example.monghyang.domain.global.pg.gateway;

import com.example.monghyang.domain.global.pg.component.Amount;
import com.example.monghyang.domain.global.pg.status.PayoutStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Payout { // 지급대행 요청 정보
    // 지급 요청의 고유 식별자
    private String id;

    // 상점에서 부여한 지급 요청의 참조 ID
    private String refPayoutId;

    // 지급 받을 셀러의 ID
    private String destination;

    // 지급 스케줄 타입 (EXPRESS: 바로지급, SCHEDULED: 예약지급)
    private String scheduleType;

    // 실제 지급일 (YYYY-MM-DD)
    private String payoutDate;

    // 지급 금액 정보 (통화, 금액)
    private Amount amount;

    // 지급 관련 설명 (예: '8월대금지급')
    private String transactionDescription;

    // 지급 요청이 생성된 일시 (ISO 8601)
    private String requestedAt;

    // 지급 요청의 상태 (REQUESTED, IN_PROGRESS, COMPLETED, FAILED, CANCELED 등)
    private PayoutStatus status;

    // 오류 정보 (지급 실패 시)
    private Object error;

    // 추가 정보(키-값 쌍)
    private Map<String, String> metadata;
}
