package com.example.monghyang.domain.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReqCartDto {
    @NotNull(message = "장바구니에 담을 상품의 식별자 값을 보내주세요.")
    private Long product_id;
    @NotNull(message = "장바구니에 담을 상품의 수량 정보를 보내주세요.")
    @Min(value = 1, message = "수량 정보는 1 이상이어야 합니다.")
    @Max(value = 99, message = "수량 정보는 100보다 작아야 합니다.")
    private Integer quantity;
}
