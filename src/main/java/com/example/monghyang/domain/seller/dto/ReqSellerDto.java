package com.example.monghyang.domain.seller.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import com.example.monghyang.domain.image.dto.AddImageDto;
import com.example.monghyang.domain.image.dto.ModifySeqImageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "판매자 정보 수정 요청 정보")
public class ReqSellerDto {
    @Schema(description = "변경할 판매자 상호명입니다. null이면 변경하지 않습니다.", example = "몽향상점", nullable = true)
    @AllowNullNotBlankString
    private String seller_name;
    @Schema(description = "변경할 판매자 주소입니다. null이면 변경하지 않습니다.", example = "서울시 중구 세종대로 110", nullable = true)
    @AllowNullNotBlankString
    private String seller_address;
    @Schema(description = "변경할 판매자 상세 주소입니다. null이면 변경하지 않습니다.", example = "101호", nullable = true)
    @AllowNullNotBlankString
    private String seller_address_detail;
    @Schema(description = "변경할 사업자등록번호입니다. null이면 변경하지 않습니다.", example = "123-45-67890", nullable = true)
    @AllowNullNotBlankString
    private String business_registration_number;
    @Schema(description = "변경할 정산 계좌번호입니다. null이면 변경하지 않습니다.", example = "110123456789", nullable = true)
    @AllowNullNotBlankString
    private String seller_account_number;
    @Schema(description = "변경할 정산 계좌 예금주입니다. null이면 변경하지 않습니다.", example = "홍길동", nullable = true)
    @AllowNullNotBlankString
    private String seller_depositor;
    @Schema(description = "변경할 정산 은행명입니다. null이면 변경하지 않습니다.", example = "신한은행", nullable = true)
    @AllowNullNotBlankString
    private String seller_bank_name;
    @Schema(description = "변경할 판매자 소개글입니다. null이면 변경하지 않습니다.", example = "전통주 상품을 판매합니다.", nullable = true)
    @AllowNullNotBlankString
    private String introduction;
    @Schema(description = "새로 추가할 판매자 이미지 목록입니다.")
    private List<AddImageDto> add_images = new ArrayList<>(); // 새로 추가할 이미지 리스트
    @Schema(description = "기존 판매자 이미지의 노출 순서 변경 목록입니다.")
    private List<ModifySeqImageDto> modify_images = new ArrayList<>(); // 위치 변경할 이미지 리스트
    @Schema(description = "삭제할 기존 판매자 이미지 식별자 목록입니다.", example = "[1, 2]")
    private List<Long> remove_images = new ArrayList<>(); // 삭제할 이미지의 식별자 값 리스트
}
