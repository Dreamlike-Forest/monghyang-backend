package com.example.monghyang.domain.orders.item.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ResOrderItemDto {
    private final Long order_id;
    private final Long order_item_id;
    private final Long product_id;
    private final String product_name;
    private final Long provider_id;
    private final String provider_nickname;
    private final String provider_role;
    private final String product_image_key;
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

    public ResOrderItemDto(OrderItemDto orderItem) {
        this.order_id = orderItem.getOrderId();
        this.order_item_id = orderItem.getOrderItemId();
        this.product_id = orderItem.getProductId();
        this.product_name = orderItem.getProductName();
        this.provider_id = orderItem.getProviderId();
        this.provider_nickname = orderItem.getProviderNickname();
        this.provider_role = orderItem.getProviderRole().getName().name();
        this.product_image_key = orderItem.getProductImageKey();
        this.order_item_quantity = orderItem.getOrderItemQuantity();
        this.order_item_amount = orderItem.getOrderItemAmount();
        this.order_item_fulfillment_status = orderItem.getOrderItemFulfillmentStatus().name();
        this.order_item_refund_status = orderItem.getOrderItemRefundStatus().name();
        this.order_item_carrier_code = orderItem.getOrderItemCarrierCode();
        this.order_item_tracking_no = orderItem.getOrderItemTrackingNo();
        this.order_item_shipped_at = orderItem.getOrderItemShippedAt();
        this.order_item_delivered_at = orderItem.getOrderItemDeliveredAt();
        this.order_item_created_at = orderItem.getOrderItemCreatedAt();
        this.order_item_updated_at = orderItem.getOrderItemUpdatedAt();
    }
}
