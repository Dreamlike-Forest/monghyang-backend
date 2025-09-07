package com.example.monghyang.domain.product.review.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqProductReviewDto {
    @NotNull(message = "상품 식별자를 첨부해주세요.")
    private Long product_id;
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String content;
    @NotNull(message = "리뷰 점수를 입력해주세요.")
    private Double star;
}
