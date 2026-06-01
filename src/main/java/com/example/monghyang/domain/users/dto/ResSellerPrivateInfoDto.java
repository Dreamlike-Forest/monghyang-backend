package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.seller.dto.ResSellerImageDto;
import com.example.monghyang.domain.seller.entity.Seller;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Schema(description = "내 정보 조회의 판매자 회원 추가 정보")
public class ResSellerPrivateInfoDto {
    @Schema(description = "판매자 식별자입니다.", example = "1")
    private final Long seller_id;
    @Schema(description = "판매자 상호명입니다.", example = "몽향상점")
    private final String seller_name;
    @Schema(description = "판매자 주소입니다.", example = "서울시 중구 세종대로 110")
    private final String seller_address;
    @Schema(description = "판매자 상세 주소입니다.", example = "101호")
    private final String seller_address_detail;
    @Schema(description = "판매자 등록일입니다.", example = "2026-06-01", type = "string", format = "date")
    private final LocalDate seller_registered_at;
    @Schema(description = "사업자등록번호입니다.", example = "123-45-67890")
    private final String seller_business_registration_number;
    @Schema(description = "정산 계좌번호입니다.", example = "110123456789")
    private final String seller_account_number;
    @Schema(description = "정산 계좌 예금주입니다.", example = "홍길동")
    private final String seller_depositor;
    @Schema(description = "정산 은행명입니다.", example = "신한은행")
    private final String seller_bank_name;
    @Schema(description = "판매자 소개글입니다.", example = "전통주 상품을 판매합니다.", nullable = true)
    private final String seller_introduction;
    @Schema(description = "판매자 약관 동의 여부입니다.", example = "true")
    private final Boolean seller_is_agreed_seller;
    @Schema(description = "판매자 삭제 처리 여부입니다.", example = "false")
    private final Boolean seller_is_deleted;
    @Schema(description = "판매자 이미지 목록입니다. seq=1이 대표 이미지입니다.")
    private final List<ResSellerImageDto> seller_images = new ArrayList<>();

    public ResSellerPrivateInfoDto(Seller seller) {
        this.seller_id = seller.getId();
        this.seller_name = seller.getSellerName();
        this.seller_address = seller.getSellerAddress();
        this.seller_address_detail = seller.getSellerAddressDetail();
        this.seller_registered_at = seller.getRegisteredAt();
        this.seller_business_registration_number = seller.getBusinessRegistrationNumber();
        this.seller_account_number = seller.getSellerAccountNumber();
        this.seller_depositor = seller.getSellerDepositor();
        this.seller_bank_name = seller.getSellerBankName();
        this.seller_introduction = seller.getIntroduction();
        this.seller_is_agreed_seller = seller.getIsAgreedSeller();
        this.seller_is_deleted = seller.getIsDeleted();
    }

}
