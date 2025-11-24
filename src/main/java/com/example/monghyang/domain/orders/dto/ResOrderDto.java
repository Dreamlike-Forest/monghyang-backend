package com.example.monghyang.domain.orders.dto;

import com.example.monghyang.domain.orders.entity.PaymentStatus;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ResOrderDto {
    private final Long order_id;
    private final BigDecimal order_total_amount;
    private final String order_currency;
    private final String order_payer_name;
    private final String order_payer_phone;
    private final PaymentStatus order_payment_status;
    private final String order_address;
    private final String order_address_detail;
    private final LocalDateTime order_created_at;
    private final LocalDateTime order_updated_at;
    private List<ResOrderItemDto> order_items = new ArrayList<>();
}
