package com.example.monghyang.domain.product.dto;

import com.example.monghyang.domain.tag.dto.TagNameDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ResMyProductDto {
    private final Long product_id;
    private final String product_name;
    private final Double product_alcohol;
    private final Integer product_volume;
    private final Integer product_sales_volume; // 판매량
    private final BigDecimal product_origin_price;
    private final BigDecimal product_discount_rate;
    private final BigDecimal product_final_price;
    private final String image_key; // 상품 대표 이미지
    private final Boolean product_is_online_sell;
    private final Boolean product_is_soldout;
    private final Boolean product_is_deleted;
    private final String product_description;
    private final LocalDateTime product_registered_at;
    private final List<TagNameDto> tags = new ArrayList<>();
}
