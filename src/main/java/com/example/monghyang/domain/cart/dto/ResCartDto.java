package com.example.monghyang.domain.cart.dto;


import com.example.monghyang.domain.cart.entity.Cart;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResCartDto {
    private Long cart_id;
    private Long product_id;
    private Integer cart_quantity;
    private LocalDateTime cart_created_at;

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
