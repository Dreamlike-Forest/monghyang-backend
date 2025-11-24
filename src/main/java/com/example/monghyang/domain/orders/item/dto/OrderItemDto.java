package com.example.monghyang.domain.orders.item.dto;

import com.example.monghyang.domain.orders.item.entity.FulfillmentStatus;
import com.example.monghyang.domain.orders.item.entity.RefundStatus;
import com.example.monghyang.domain.users.entity.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderItemDto {
    Long getOrderId();
    Long getOrderItemId();
    Long getProductId();
    String getProductName();
    Long getProviderId();
    String getProviderNickname();
    Role getProviderRole();
    String getProductImageKey();
    Integer getOrderItemQuantity();
    BigDecimal getOrderItemAmount();
    FulfillmentStatus getOrderItemFulfillmentStatus();
    RefundStatus getOrderItemRefundStatus();
    String getOrderItemCarrierCode();
    String getOrderItemTrackingNo();
    LocalDateTime getOrderItemShippedAt();
    LocalDateTime getOrderItemDeliveredAt();
    LocalDateTime getOrderItemCreatedAt();
    LocalDateTime getOrderItemUpdatedAt();


}
