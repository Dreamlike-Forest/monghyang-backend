package com.example.monghyang.domain.orders.item.dto;

import com.example.monghyang.domain.orders.item.entity.OrderItem;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ResOrderItemDto {
    private final Long order_item_id;
    private final Long product_id;
    private final Long provider_id;
    private final Integer order_item_quantity;
    private final BigDecimal order_item_amount;
    private final String order_item_fulfillment_status;
    private final String order_item_refund_status;
    private final String order_item_carrier_code;
    private final String order_item_tracking_no;
    private final LocalDateTime order_item_shipped_at;
    private final LocalDateTime order_item_delivered_at;
    private final LocalDateTime order_item_created_at;
    private final LocalDateTime order_item_updated_at;

    public ResOrderItemDto(OrderItem orderItem) {
        this.order_item_id = orderItem.getId();
        this.product_id = orderItem.getProduct().getId();
        this.provider_id = orderItem.getProvider().getId();
        this.order_item_quantity = orderItem.getQuantity();
        this.order_item_amount = orderItem.getAmount();
        this.order_item_fulfillment_status = orderItem.getFulfillmentStatus().name();
        this.order_item_refund_status = orderItem.getRefundStatus().name();
        this.order_item_carrier_code = orderItem.getCarrierCode();
        this.order_item_tracking_no = orderItem.getTrackingNo();
        this.order_item_shipped_at = orderItem.getShippedAt();
        this.order_item_delivered_at = orderItem.getDeliveredAt();
        this.order_item_created_at = orderItem.getCreatedAt();
        this.order_item_updated_at = orderItem.getUpdatedAt();
    }
}
