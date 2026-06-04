package com.example.monghyang.domain.orders.dto;

import com.example.monghyang.domain.orders.entity.PaymentStatus;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Schema(description = "상품 주문 응답 정보")
public class ResOrderDto {
    @Schema(description = "주문 식별자입니다.", example = "1")
    private final Long order_id;
    @Schema(description = "주문 총 결제 금액입니다.", example = "59000.00")
    private final BigDecimal order_total_amount;
    @Schema(description = "결제 통화 코드입니다.", example = "KRW")
    private final String order_currency;
    @Schema(description = "주문자명입니다.", example = "홍길동")
    private final String order_payer_name;
    @Schema(description = "주문자 연락처입니다.", example = "010-1234-5678")
    private final String order_payer_phone;
    @Schema(description = "주문 결제 상태입니다.", example = "PAID")
    private final PaymentStatus order_payment_status;
    @Schema(description = "배송지 기본 주소입니다.", example = "서울시 중구 세종대로 110")
    private final String order_address;
    @Schema(description = "배송지 상세 주소입니다.", example = "101호")
    private final String order_address_detail;
    @Schema(description = "주문 생성 시각입니다.", example = "2026-06-04T12:00:00")
    private final LocalDateTime order_created_at;
    @Schema(description = "주문 수정 시각입니다.", example = "2026-06-04T12:10:00")
    private final LocalDateTime order_updated_at;
    @Schema(description = "주문에 포함된 상품 주문 요소 목록입니다.")
    private List<ResOrderItemDto> order_items = new ArrayList<>();
}
