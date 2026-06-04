package com.example.monghyang.domain.orders.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "주문 요소 배송/환불 상태 변경 이력 응답 정보")
public class ResOrderItemStatusHistoryDto {
    @Schema(description = "배송 처리 상태 변경 이력 목록입니다.")
    private List<ResFulfillmentStatusHistoryDto> fulfillmentHistory = new ArrayList<>();
    @Schema(description = "환불 처리 상태 변경 이력 목록입니다.")
    private List<ResRefundStatusHistoryDto> refundHistory = new ArrayList<>();
}
