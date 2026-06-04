package com.example.monghyang.domain.joy.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "체험 리뷰 수정 요청 정보")
public class ReqUpdateJoyReviewDto {
    @Schema(description = "변경할 체험 리뷰 본문입니다. null이면 변경하지 않습니다.", example = "막걸리 빚기 체험이 친절하고 좋았습니다.", nullable = true)
    private String content;
    @Schema(description = "변경할 체험 리뷰 별점입니다. null이면 변경하지 않습니다.", example = "4.5", nullable = true)
    private Double star;
}
