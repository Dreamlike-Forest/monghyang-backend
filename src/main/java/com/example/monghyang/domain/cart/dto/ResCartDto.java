package com.example.monghyang.domain.cart.dto;


import com.example.monghyang.domain.cart.entity.Cart;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "장바구니 요소 응답 정보")
public class ResCartDto {
    @Schema(description = "장바구니 요소 식별자입니다.", example = "1")
    private final Long cart_id;
    @Schema(description = "상품 식별자입니다.", example = "10")
    private final Long product_id;
    @Schema(description = "장바구니에 담긴 상품 수량입니다.", example = "2")
    private final Integer cart_quantity;
    @Schema(description = "장바구니 요소 생성 시각입니다.", example = "2026-06-04T12:00:00")
    private final LocalDateTime cart_created_at;

    private ResCartDto(Cart cart) {
        this.cart_id = cart.getId();
        this.product_id = cart.getProduct().getId();
        this.cart_quantity = cart.getQuantity();
        this.cart_created_at = cart.getCreatedAt();
    }
    public static ResCartDto cartFrom(Cart cart) {
        return new ResCartDto(cart);
    }
}
