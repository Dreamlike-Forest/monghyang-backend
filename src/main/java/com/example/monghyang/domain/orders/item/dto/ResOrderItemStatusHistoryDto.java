package com.example.monghyang.domain.orders.item.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ResOrderItemStatusHistoryDto {
    List<ResFulfillmentStatusHistoryDto> fulfillmentHistory = new ArrayList<>();
    List<ResRefundStatusHistoryDto> refundHistory = new ArrayList<>();
}
