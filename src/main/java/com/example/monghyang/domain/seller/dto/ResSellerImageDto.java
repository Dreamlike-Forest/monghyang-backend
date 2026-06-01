package com.example.monghyang.domain.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "판매자 이미지 응답 정보")
public record ResSellerImageDto(
        @Schema(description = "판매자 이미지 식별자입니다.", example = "1")
        Long seller_image_id,
        @Schema(description = "이미지 파일 key입니다. 실제 이미지 조회 API에 전달할 수 있습니다.", example = "seller/1/main.jpg")
        String seller_image_image_key,
        @Schema(description = "이미지 노출 순서입니다. seq=1이 대표 이미지입니다.", example = "1")
        Integer seller_image_seq
) {
}
