package com.example.monghyang.domain.orders.item.dto;

import com.example.monghyang.domain.orders.item.entity.OrderItemFulfillmentHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "주문 요소 배송 처리 상태 변경 이력")
public class ResFulfillmentStatusHistoryDto {
    @Schema(description = "배송 처리 상태 변경 이력 식별자입니다.", example = "1")
    private final Long order_item_fulfillment_history_id;
    @Schema(description = "변경 후 배송 처리 상태입니다.", example = "SHIPPED")
    private final String order_item_fulfillment_history_to_status;
    @Schema(description = "배송 처리 상태 변경 사유 코드입니다.", example = "SHIP_START", nullable = true)
    private final String order_item_fulfillment_history_reason_code;
    @Schema(description = "배송 처리 상태 변경 시각입니다.", example = "2026-06-05T10:00:00")
    private final LocalDateTime order_item_fulfillment_history_created_at;

    public ResFulfillmentStatusHistoryDto(OrderItemFulfillmentHistory history) {
        this.order_item_fulfillment_history_id = history.getId();
        this.order_item_fulfillment_history_to_status = history.getToStatus().name();
        this.order_item_fulfillment_history_reason_code = history.getReasonCode();
        this.order_item_fulfillment_history_created_at = history.getCreatedAt();
    }
}
