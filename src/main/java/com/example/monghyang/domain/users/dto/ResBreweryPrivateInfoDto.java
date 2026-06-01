package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.brewery.dto.ResBreweryImageDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Schema(description = "내 정보 조회의 양조장 회원 추가 정보")
public class ResBreweryPrivateInfoDto {
    @Schema(description = "양조장 식별자입니다.", example = "1")
    private final Long brewery_id;
    @Schema(description = "지역 식별자입니다.", example = "1")
    private final Integer region_type_id;
    @Schema(description = "지역명입니다.", example = "서울")
    private final String region_type_name;
    @Schema(description = "양조장 상호명입니다.", example = "몽향양조장")
    private final String brewery_name;
    @Schema(description = "양조장 주소입니다.", example = "서울시 중구 세종대로 110")
    private final String brewery_address;
    @Schema(description = "양조장 상세 주소입니다.", example = "101호")
    private final String brewery_address_detail;
    @Schema(description = "양조장 등록일입니다.", example = "2026-06-01", type = "string", format = "date")
    private final LocalDate brewery_registered_at;
    @Schema(description = "사업자등록번호입니다.", example = "123-45-67890")
    private final String brewery_business_registration_number;
    @Schema(description = "정산 계좌 예금주입니다.", example = "홍길동")
    private final String brewery_depositor;
    @Schema(description = "정산 계좌번호입니다.", example = "110123456789")
    private final String brewery_account_number;
    @Schema(description = "정산 은행명입니다.", example = "신한은행")
    private final String brewery_bank_name;
    @Schema(description = "양조장 소개글입니다.", example = "전통주 체험을 운영하는 양조장입니다.", nullable = true)
    private final String brewery_introduction;
    @Schema(description = "양조장 웹사이트 주소입니다.", example = "https://example.com", nullable = true)
    private final String brewery_website;
    @Schema(description = "정기 방문 가능 여부입니다.", example = "true")
    private final Boolean brewery_is_regular_visit;
    @Schema(description = "방문 양조장 여부입니다.", example = "true")
    private final Boolean brewery_is_visiting_brewery;
    @Schema(description = "양조장 회원 약관 동의 여부입니다.", example = "true")
    private final Boolean brewery_is_agreed_brewery;
    @Schema(description = "양조장 삭제 처리 여부입니다.", example = "false")
    private final Boolean brewery_is_deleted;
    @Schema(description = "양조장 이미지 목록입니다. seq=1이 대표 이미지입니다.")
    private final List<ResBreweryImageDto> brewery_images = new ArrayList<>();

    /**
     * 생성자
     * @param brewery region_type과 join fetch 한 brewery
     */
    public ResBreweryPrivateInfoDto(Brewery brewery) {
        this.brewery_id = brewery.getId();
        this.region_type_id = brewery.getRegionType().getId();
        this.region_type_name = brewery.getRegionType().getName();
        this.brewery_name = brewery.getBreweryName();
        this.brewery_address = brewery.getBreweryAddress();
        this.brewery_address_detail = brewery.getBreweryAddressDetail();
        this.brewery_registered_at = brewery.getRegisteredAt();
        this.brewery_business_registration_number = brewery.getBusinessRegistrationNumber();
        this.brewery_depositor = brewery.getBreweryDepositor();
        this.brewery_account_number = brewery.getBreweryAccountNumber();
        this.brewery_bank_name = brewery.getBreweryBankName();
        this.brewery_introduction = brewery.getIntroduction();
        this.brewery_website = brewery.getBreweryWebsite();
        this.brewery_is_regular_visit = brewery.getIsRegularVisit();
        this.brewery_is_visiting_brewery = brewery.getIsVisitingBrewery();
        this.brewery_is_agreed_brewery = brewery.getIsAgreedBrewery();
        this.brewery_is_deleted = brewery.getIsDeleted();
    }
}
