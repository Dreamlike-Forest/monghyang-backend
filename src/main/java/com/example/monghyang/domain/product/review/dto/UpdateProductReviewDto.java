package com.example.monghyang.domain.product.review.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductReviewDto {
    @NotNull(message = "수정할 상품 리뷰의 식별자를 입력해주세요.")
    private Long id;
    private String content;
    private Double star;
}
