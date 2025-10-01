package com.example.monghyang.domain.joy.dto;

import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ResJoyOrderDto {
    private final Long joy_order_id;
    private final Long user_id;
    private final Long joy_id;
    private final String joy_name;
    private final Integer joy_order_count;
    private final BigDecimal joy_total_price;
    private final String joy_order_payer_name;
    private final String joy_order_payer_phone;
    private final LocalDateTime joy_order_created_at;
    private final LocalDateTime joy_order_reservation;
    private final JoyPaymentStatus joy_payment_status;
}
