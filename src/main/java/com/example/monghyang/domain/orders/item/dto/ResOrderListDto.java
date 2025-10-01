package com.example.monghyang.domain.orders.item.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ResOrderListDto {
    private final Long order_id;
    private final LocalDateTime order_created_at;
    private List<ResOrderItemDto> order_item_list = new ArrayList<>();

}
