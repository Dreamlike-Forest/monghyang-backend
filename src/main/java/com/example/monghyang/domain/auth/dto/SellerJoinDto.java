package com.example.monghyang.domain.auth.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "판매자 회원가입 요청 정보")
public class SellerJoinDto extends JoinDto {
    @Schema(description = "판매자 사업자등록번호입니다.", example = "123-45-67890", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "business_registration_number 값이 공백일 수 없습니다.")
    private String business_registration_number;
    @Schema(description = "판매자 정산 계좌번호입니다.", example = "110123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "seller_account_number 값이 공백일 수 없습니다.")
    private String seller_account_number;
    @Schema(description = "판매자 정산 계좌 예금주입니다.", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "seller_depositor 값이 공백일 수 없습니다.")
    private String seller_depositor;
    @Schema(description = "판매자 정산 은행명입니다.", example = "신한은행", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "seller_bank_name 값이 공백일 수 없습니다.")
    private String seller_bank_name;
    @Schema(description = "판매자 소개글입니다. null은 허용하지만 빈 문자열은 허용하지 않습니다.", example = "전통주 상품을 판매합니다.", nullable = true)
    @AllowNullNotBlankString
    private String introduction;
    @Schema(description = "판매자 약관 동의 여부입니다. 회원가입하려면 true여야 합니다.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "is_agreed_seller 필드가 공백일 수 없습니다.")
    private Boolean is_agreed_seller;

    @Schema(description = "판매자 이미지 목록입니다. 최대 5개까지 전달할 수 있고, seq=1인 이미지가 대표 이미지입니다.")
    @Size(max = 5, message = "이미지는 최대 5개까지 추가할 수 있습니다.")
    private List<AddImageDto> images; // 새로 추가할 이미지 파일 + 순서 정보 쌍의 리스트
}
