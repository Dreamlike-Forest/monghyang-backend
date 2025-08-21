package com.example.monghyang.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerJoinDto extends JoinDto {
    @NotBlank(message = "business_registration_number 값이 공백일 수 없습니다.")
    private String business_registration_number;
    @NotBlank(message = "seller_account_number 값이 공백일 수 없습니다.")
    private String seller_account_number;
    @NotBlank(message = "seller_depositor 값이 공백일 수 없습니다.")
    private String seller_depositor;
    @NotBlank(message = "seller_bank_name 값이 공백일 수 없습니다.")
    private String seller_bank_name;
    private String introduction;
    @NotNull(message = "is_agreed_seller 필드가 공백일 수 없습니다.")
    private Boolean is_agreed_seller;
}
