package com.example.monghyang.domain.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "상품 주문 결제 준비 요청 정보")
public class ReqPreOrderDto {
    @Schema(description = "주문할 장바구니 요소 식별자 목록입니다.", example = "[1, 2]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "주문하려는 장바구니 요소의 식별자를 보내주세요.")
    private List<Long> cart_id = new ArrayList<>();
    @Schema(description = "주문자명입니다.", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "주문자명을 입력해주세요.")
    private String payer_name;
    @Schema(description = "주문자 연락처입니다.", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "주문자 전화번호를 입력해주세요.")
    private String payer_phone;
    @Schema(description = "배송지 기본 주소입니다.", example = "서울시 중구 세종대로 110", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "배송지 주소를 입력해주세요.")
    private String address;
    @Schema(description = "배송지 상세 주소입니다.", example = "101호", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "배송지 주소 상세정보를 입력해주세요.")
    private String address_detail;
}
