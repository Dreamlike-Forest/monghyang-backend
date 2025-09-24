package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.product.entity.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ResProductDto {
    private Long product_id;
    private String product_name;
    private Double product_alcohol;
    private Integer product_sales_volume;
    private Integer product_volume;
    private String product_description;
    private LocalDateTime product_registered_at;
    private BigDecimal product_final_price;
    private BigDecimal product_discount_rate;
    private BigDecimal product_origin_price;

    private String user_nickname; // 상호명
    private List<ResProductImageDto> product_image_image_key; // 상품 이미지 리스트
    private List<String> tags_name; // 상품 태그 리스트
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
    }

    public static ResProductDto productFrom(Product product) {
        return new ResProductDto(product);
    }

    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public void setProduct_image_image_key(List<ResProductImageDto> product_image_image_key) {
        this.product_image_image_key = product_image_image_key;
    }

    public void setTags_name(List<String> tags_name) {
        this.tags_name = tags_name;
    }

    public void setOwner(ResProductOwnerDto owner) {
        this.owner = owner;
    }
}