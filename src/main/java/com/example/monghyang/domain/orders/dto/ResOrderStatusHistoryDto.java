package com.example.monghyang.domain.orders.dto;

import com.example.monghyang.domain.orders.entity.OrderStatusHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "상품 주문 상태 변경 이력 응답 정보")
public class ResOrderStatusHistoryDto {
    @Schema(description = "주문 상태 변경 이력 식별자입니다.", example = "1")
    private final Long order_status_history_id;
    @Schema(description = "변경 후 주문 상태입니다.", example = "PAID")
    private final String order_status_history_to_status;
    @Schema(description = "상태 변경 사유 코드입니다.", example = "PAYMENT_APPROVED", nullable = true)
    private final String order_status_history_reason_code;
    @Schema(description = "상태 변경 이력 생성 시각입니다.", example = "2026-06-04T12:00:00")
    private final LocalDateTime order_status_history_created_at;

    public ResOrderStatusHistoryDto(OrderStatusHistory orderStatusHistory) {
        this.order_status_history_id = orderStatusHistory.getId();
        this.order_status_history_to_status = orderStatusHistory.getToStatus().name();
        this.order_status_history_reason_code = orderStatusHistory.getReasonCode();
        this.order_status_history_created_at = orderStatusHistory.getCreatedAt();
    }
}
