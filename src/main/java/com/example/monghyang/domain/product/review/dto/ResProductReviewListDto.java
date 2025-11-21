package com.example.monghyang.domain.product.review.dto;

import com.example.monghyang.domain.product.review.ProductReview;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResProductReviewListDto {
    private Long product_review_id;
    private String users_name;
    private String product_review_content;
    private Double product_review_star;
    private LocalDateTime product_review_created_at;

    private ResProductReviewListDto(ProductReview productReview) {
        this.product_review_id = productReview.getId();
        this.users_name = productReview.getUser().getName();
        this.product_review_content = productReview.getContent();
        this.product_review_star = productReview.getStar();
        this.product_review_created_at = productReview.getCreatedAt();
    }

    public static ResProductReviewListDto productReviewFrom(ProductReview productReview) {
        return new ResProductReviewListDto(productReview);
    }
}
