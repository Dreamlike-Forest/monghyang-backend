package com.example.monghyang.domain.orders.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Schema(description = "상품 주문 요소 응답 정보")
public class ResOrderItemDto {
    @Schema(description = "주문 식별자입니다.", example = "1")
    private final Long order_id;
    @Schema(description = "주문 요소 식별자입니다.", example = "10")
    private final Long order_item_id;
    @Schema(description = "상품 식별자입니다.", example = "3")
    private final Long product_id;
    @Schema(description = "상품명입니다.", example = "전통주 선물세트")
    private final String product_name;
    @Schema(description = "상품 제공자 회원 식별자입니다.", example = "20")
    private final Long provider_id;
    @Schema(description = "상품 제공자 닉네임 또는 상호명입니다.", example = "몽향상점")
    private final String provider_nickname;
    @Schema(description = "상품 제공자 역할입니다.", example = "ROLE_SELLER")
    private final String provider_role;
    @Schema(description = "상품 대표 이미지 key입니다.", example = "product/3/main.jpg", nullable = true)
    private final String product_image_key;
    @Schema(description = "주문 수량입니다.", example = "2")
    private final Integer order_item_quantity;
    @Schema(description = "주문 요소 금액입니다.", example = "59000.00")
    private final BigDecimal order_item_amount;
    @Schema(description = "주문 요소 배송 처리 상태입니다.", example = "PAID")
    private final String order_item_fulfillment_status;
    @Schema(description = "주문 요소 환불 처리 상태입니다.", example = "NONE")
    private final String order_item_refund_status;
    @Schema(description = "택배사 코드입니다.", example = "CJ", nullable = true)
    private final String order_item_carrier_code;
    @Schema(description = "운송장 번호입니다.", example = "1234567890", nullable = true)
    private final String order_item_tracking_no;
    @Schema(description = "배송 시작 시각입니다.", example = "2026-06-05T10:00:00", nullable = true)
    private final LocalDateTime order_item_shipped_at;
    @Schema(description = "배송 완료 시각입니다.", example = "2026-06-06T18:00:00", nullable = true)
    private final LocalDateTime order_item_delivered_at;
    @Schema(description = "주문 요소 생성 시각입니다.", example = "2026-06-04T12:00:00")
    private final LocalDateTime order_item_created_at;
    @Schema(description = "주문 요소 수정 시각입니다.", example = "2026-06-04T12:10:00")
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
