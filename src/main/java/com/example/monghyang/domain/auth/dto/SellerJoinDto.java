package com.example.monghyang.domain.auth.dto;

import com.example.monghyang.domain.image.dto.AddImageDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    private List<AddImageDto> images; // 새로 추가할 이미지 파일 + 순서 정보 쌍의 리스트
}
