package com.example.monghyang.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BreweryJoinDto extends JoinDto{
    @NotBlank(message = "brewery_name 값이 공백일 수 없습니다.")
    private String brewery_name;
    @NotBlank(message = "brewery_phone 값이 공백일 수 없습니다.")
    private String brewery_phone;
    @NotBlank(message = "brewery_email 값이 공백일 수 없습니다.")
    @Email(message = "brewery_email 필드에 이메일 형식의 값을 입력하세요.")
    private String brewery_email;
    @NotBlank(message = "brewery_address 값이 공백일 수 없습니다.")
    private String brewery_address;
    @NotBlank(message = "brewery_address_detail 값이 공백일 수 없습니다.")
    private String brewery_address_detail;
    @NotBlank(message = "business_registration_number 값이 공백일 수 없습니다.")
    private String business_registration_number;
    @NotBlank(message = "brewery_depositor 값이 공백일 수 없습니다.")
    private String brewery_depositor;
    @NotBlank(message = "brewery_account_number 값이 공백일 수 없습니다.")
    private String brewery_account_number;
    @NotBlank(message = "brewery_bank_name 값이 공백일 수 없습니다.")
    private String brewery_bank_name;
    private String introduction;
    private String brewery_website;

    @NotNull(message = "region_type_id 값이 공백일 수 없습니다.")
    private Integer region_type_id;

    @NotNull(message = "is_regular_visit 값이 공백일 수 없습니다.")
    private Boolean is_regular_visit;
    @NotNull(message = "is_agreed_brewery 값이 공백일 수 없습니다.")
    private Boolean is_agreed_brewery;
}
