package com.example.monghyang.domain.orders.item.dto;

import com.example.monghyang.domain.orders.item.entity.OrderItemRefundHistory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResRefundStatusHistoryDto {
    private final Long order_item_refund_history_id;
    private final String order_item_refund_to_status;
    private final LocalDateTime order_item_refund_created_at;

    public ResRefundStatusHistoryDto(OrderItemRefundHistory history) {
        this.order_item_refund_history_id = history.getId();
        this.order_item_refund_to_status = history.getToStatus().name();
        this.order_item_refund_created_at = history.getCreatedAt();
    }
}
