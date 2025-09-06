package com.example.monghyang.domain.product.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ResProductListDto {
    private final Long product_id;
    private final String users_nickname; // 판매자 상호명
    private final String product_name;
    private final Double product_review_star; // 상품의 평균 평점
    private final Long product_review_count; // 상품의 리뷰 개수
    private final Double product_alcohol;
    private final Integer product_volume;
    private final Integer product_sales_volume; // 판매량
    private final Integer product_origin_price;
    private final Integer product_discount_rate;
    private final Integer product_final_price;
    private final String image_key; // 상품 대표 이미지
    @Setter
    private List<String> tag_name;
}
