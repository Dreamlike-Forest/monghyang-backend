package com.example.monghyang.domain.orders.item.dto;

import com.example.monghyang.domain.orders.item.entity.OrderItemFulfillmentHistory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResFulfillmentStatusHistoryDto {
    private final Long order_item_fulfillment_history_id;
    private final String order_item_fulfillment_history_to_status;
    private final String order_item_fulfillment_history_reason_code;
    private final LocalDateTime order_item_fulfillment_history_created_at;

    public ResFulfillmentStatusHistoryDto(OrderItemFulfillmentHistory history) {
        this.order_item_fulfillment_history_id = history.getId();
        this.order_item_fulfillment_history_to_status = history.getToStatus().name();
        this.order_item_fulfillment_history_reason_code = history.getReasonCode();
        this.order_item_fulfillment_history_created_at = history.getCreatedAt();
    }
}
