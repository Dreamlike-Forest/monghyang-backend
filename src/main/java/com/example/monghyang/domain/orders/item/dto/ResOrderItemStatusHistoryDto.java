package com.example.monghyang.domain.orders.item.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ResOrderItemStatusHistoryDto {
    private List<ResFulfillmentStatusHistoryDto> fulfillmentHistory = new ArrayList<>();
    private List<ResRefundStatusHistoryDto> refundHistory = new ArrayList<>();
}
