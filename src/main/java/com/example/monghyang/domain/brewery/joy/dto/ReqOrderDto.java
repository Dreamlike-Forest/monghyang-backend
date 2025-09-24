package com.example.monghyang.domain.brewery.joy.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReqOrderDto {
    @NotNull(message = "본 서버로부터 발급받았던 pg_order_id를 입력해주세요.")
    private UUID pg_order_id;
    @NotNull(message = "PG사로부터 발급받은 PG Payment Key를 입력해주세요.")
    private String pg_payment_key;
    @NotNull(message = "총 결제 금액 값을 입력해주세요.")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal total_price;
}
