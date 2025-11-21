package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.product.entity.Product;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ResProductDto {
    private final Long product_id;
    private final String product_name;
    private final Double product_alcohol;
    private final Integer product_sales_volume;
    private final Integer product_volume;
    private final String product_description;
    private final LocalDateTime product_registered_at;
    private final BigDecimal product_final_price;
    private final BigDecimal product_discount_rate;
    private final BigDecimal product_origin_price;
    private final Boolean product_is_online_sell;
    private final Boolean product_is_soldout;

    @Setter
    private String user_nickname; // 상호명
    @Setter
    private List<ResProductImageDto> product_image_image_key; // 상품 이미지 리스트
    @Setter
    private List<String> tags_name; // 상품 태그 리스트
    @Setter
    private ResProductOwnerDto owner; // 판매자/양조장의 간단 정보

    private ResProductDto(Product product) {
        this.product_id = product.getId();
        this.product_name = product.getName();
        this.product_alcohol = product.getAlcohol();
        this.product_sales_volume = product.getSalesVolume();
        this.product_volume = product.getVolume();
        this.product_description = product.getDescription();
        this.product_registered_at = product.getRegisteredAt();
        this.product_final_price = product.getFinalPrice();
        this.product_discount_rate = product.getDiscountRate();
        this.product_origin_price = product.getOriginPrice();
        this.product_is_online_sell = product.getIsOnlineSell();
        this.product_is_soldout = product.getIsSoldout();
    }

    public static ResProductDto productFrom(Product product) {
        return new ResProductDto(product);
    }

}