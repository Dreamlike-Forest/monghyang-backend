package com.example.monghyang.domain.orders.item.dto;

import com.example.monghyang.domain.orders.item.entity.FulfillmentStatus;
import com.example.monghyang.domain.orders.item.entity.RefundStatus;
import com.example.monghyang.domain.users.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ResOrderItemForSellerDto extends ResOrderItemDto {
    // 자신이 판매중인 상품에 대한 모든 주문 요소 조회: 판매자용

    private final String payer_name;
    private final String payer_phone;
    private final String payer_address;
    private final String payer_address_detail;
    @Setter
    private ResOrderItemStatusHistoryDto status_history;

    public ResOrderItemForSellerDto(Long order_id, Long order_item_id, Long product_id
            , String product_name, Long provider_id, String provider_nickname, Role provider_role
            , String product_image_key, Integer order_item_quantity, BigDecimal order_item_amount
            , FulfillmentStatus order_item_fulfillment_status, RefundStatus order_item_refund_status, String order_item_carrier_code
            , String order_item_tracking_no, LocalDateTime order_item_shipped_at, LocalDateTime order_item_delivered_at
            , LocalDateTime order_item_created_at, LocalDateTime order_item_updated_at
            , String payer_name, String payer_phone, String payer_address, String payer_address_detail) {

        super(order_id, order_item_id, product_id, product_name, provider_id, provider_nickname
                , provider_role.getName().name(), product_image_key, order_item_quantity, order_item_amount
                , order_item_fulfillment_status.name(), order_item_refund_status.name(), order_item_carrier_code
                , order_item_tracking_no, order_item_shipped_at, order_item_delivered_at, order_item_created_at
                , order_item_updated_at);
        this.payer_name = payer_name;
        this.payer_phone = payer_phone;
        this.payer_address = payer_address;
        this.payer_address_detail = payer_address_detail;
        this.status_history = new ResOrderItemStatusHistoryDto(); // 빈 객체 생성
    }
}
