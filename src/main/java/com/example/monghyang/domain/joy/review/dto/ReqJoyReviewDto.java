package com.example.monghyang.domain.joy.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "체험 리뷰 작성 요청 정보")
public class ReqJoyReviewDto {
    @Schema(description = "리뷰를 작성할 체험 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 댓글형 리뷰 식별자를 입력해주세요.")
    private Long joy_id;
    @Schema(description = "리뷰 작성 자격을 확인할 체험 예약 내역 식별자입니다.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 예약(주문)내역 식별자를 입력해주세요.")
    private Long joy_order_id;
    @Schema(description = "체험 리뷰 본문입니다.", example = "막걸리 빚기 체험이 친절하고 좋았습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "리뷰 본문을 입력해주세요.")
    private String content;
    @Schema(description = "체험 리뷰 별점입니다. 0.0~5.0 범위의 0.5 단위 값만 허용됩니다.", example = "4.5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "별점을 입력해주세요.")
    private Double star;
}
