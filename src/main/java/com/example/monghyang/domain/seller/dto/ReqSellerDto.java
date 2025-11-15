package com.example.monghyang.domain.seller.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSellerDto {
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String seller_name;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String seller_address;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String seller_address_detail;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String business_registration_number;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String seller_account_number;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String seller_depositor;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String seller_bank_name;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String introduction;
}
