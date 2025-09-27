package com.example.monghyang.domain.orders.dto;

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
public class ReqProductPreOrderDto {
    @NotEmpty(message = "주문하려는 장바구니 요소의 식별자를 보내주세요.")
    private List<Long> cart_id = new ArrayList<>();
    @NotBlank(message = "주문자명을 입력해주세요.")
    private String payer_name;
    @NotBlank(message = "주문자 전화번호를 입력해주세요.")
    private String payer_phone;
    @NotBlank(message = "배송지 주소를 입력해주세요.")
    private String address;
    @NotBlank(message = "배송지 주소 상세정보를 입력해주세요.")
    private String address_detail;
}
