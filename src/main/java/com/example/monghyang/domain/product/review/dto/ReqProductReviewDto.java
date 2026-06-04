package com.example.monghyang.domain.product.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "상품 리뷰 작성 요청 정보")
public class ReqProductReviewDto {
    @Schema(description = "리뷰를 작성할 상품 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "상품 식별자를 첨부해주세요.")
    private Long product_id;
    @Schema(description = "상품 리뷰 내용입니다.", example = "향이 좋고 배송이 빨랐습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String content;
    @Schema(description = "상품 리뷰 별점입니다.", example = "4.5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "리뷰 점수를 입력해주세요.")
    private Double star;
}
