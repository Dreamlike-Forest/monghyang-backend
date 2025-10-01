package com.example.monghyang.domain.orders.dto;

import com.example.monghyang.domain.orders.entity.Orders;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ResOrderDto {
    private final Long order_id;
    private final BigDecimal order_total_amount;
    private final String order_currency;
    private final String order_payer_name;
    private final String order_payer_phone;
    private final String order_payment_status;
    private final String order_address;
    private final String order_address_detail;
    private final LocalDateTime order_created_at;
    private final LocalDateTime order_updated_at;
    private List<ResOrderItemDto> order_items;

    public ResOrderDto(Orders orders) {
        this.order_id = orders.getId();
        this.order_total_amount = orders.getTotalAmount();
        this.order_currency = orders.getCurrency();
        this.order_payer_name = orders.getPayerName();
        this.order_payer_phone = orders.getPayerPhone();
        this.order_payment_status = orders.getPaymentStatus().name();
        this.order_address = orders.getAddress();
        this.order_address_detail = orders.getAddressDetail();
        this.order_created_at = orders.getCreatedAt();
        this.order_updated_at = orders.getUpdatedAt();
    }
    public void setOrder_items(List<ResOrderItemDto> order_items) {
        this.order_items = order_items;
    }
}
