package com.example.monghyang.domain.orders.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReqProductPreOrderDto {
    @NotEmpty(message = "주문하려는 장바구니 요소의 식별자를 보내주세요.")
    private List<Long> cart_id = new ArrayList<>();
}
