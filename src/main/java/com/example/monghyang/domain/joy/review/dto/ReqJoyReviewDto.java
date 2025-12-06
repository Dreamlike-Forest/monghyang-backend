package com.example.monghyang.domain.joy.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqJoyReviewDto {
    @NotNull(message = "체험 댓글형 리뷰 식별자를 입력해주세요.")
    private Long joy_id;
    @NotBlank(message = "리뷰 본문을 입력해주세요.")
    private String content;
    @NotNull(message = "별점을 입력해주세요.")
    private Double star;
}
