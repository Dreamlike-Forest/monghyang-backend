package com.example.monghyang.domain.product.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "상품 리뷰 수정 요청 정보")
public class UpdateProductReviewDto {
    @Schema(description = "수정할 상품 리뷰 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "수정할 상품 리뷰의 식별자를 입력해주세요.")
    private Long id;
    @Schema(description = "변경할 리뷰 내용입니다. null이면 변경하지 않습니다.", example = "향이 좋고 배송이 빨랐습니다.", nullable = true)
    private String content;
    @Schema(description = "변경할 리뷰 별점입니다. null이면 변경하지 않습니다.", example = "4.5", nullable = true)
    private Double star;
}
