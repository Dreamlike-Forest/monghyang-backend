package com.example.monghyang.domain.orders.dto;

import com.example.monghyang.domain.orders.entity.OrderStatusHistory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResOrderStatusHistoryDto {
    private final Long order_status_history_id;
    private final String order_status_history_to_status;
    private final String order_status_history_reason_code;
    private final LocalDateTime order_status_history_created_at;

    public ResOrderStatusHistoryDto(OrderStatusHistory orderStatusHistory) {
        this.order_status_history_id = orderStatusHistory.getId();
        this.order_status_history_to_status = orderStatusHistory.getToStatus().name();
        this.order_status_history_reason_code = orderStatusHistory.getReasonCode();
        this.order_status_history_created_at = orderStatusHistory.getCreatedAt();
    }
}
