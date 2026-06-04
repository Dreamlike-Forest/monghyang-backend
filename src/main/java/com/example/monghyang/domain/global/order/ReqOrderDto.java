package com.example.monghyang.domain.global.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "PG 결제 승인 요청 정보")
public class ReqOrderDto {
    @Schema(description = "서버의 결제 준비 API에서 발급한 주문 식별자입니다.", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "본 서버로부터 발급받았던 pg_order_id를 입력해주세요.")
    private UUID pg_order_id;
    @Schema(description = "PG사 결제 요청 완료 후 PG사에서 발급한 payment key입니다.", example = "pay_1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "PG사로부터 발급받은 PG Payment Key를 입력해주세요.")
    private String pg_payment_key;
    @Schema(description = "클라이언트가 결제한 총 금액입니다. 서버에 저장된 주문 금액과 다르면 결제 승인이 거부됩니다.", example = "59000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "총 결제 금액 값을 입력해주세요.")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal total_amount;
}
