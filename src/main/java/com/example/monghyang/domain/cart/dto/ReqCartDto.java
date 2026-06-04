package com.example.monghyang.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "장바구니 추가 요청 정보")
public class ReqCartDto {
    @Schema(description = "장바구니에 담을 상품 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "장바구니에 담을 상품의 식별자 값을 보내주세요.")
    private Long product_id;
    @Schema(description = "장바구니에 담을 상품 수량입니다. 1 이상 99 이하만 허용됩니다.", example = "2", minimum = "1", maximum = "99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "장바구니에 담을 상품의 수량 정보를 보내주세요.")
    @Min(value = 1, message = "수량 정보는 1 이상이어야 합니다.")
    @Max(value = 99, message = "수량 정보는 100보다 작아야 합니다.")
    private Integer quantity;
}
