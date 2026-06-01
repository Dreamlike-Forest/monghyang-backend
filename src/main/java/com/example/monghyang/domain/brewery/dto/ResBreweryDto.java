package com.example.monghyang.domain.brewery.dto;

import com.example.monghyang.domain.joy.dto.ResJoyDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.product.dto.ResProductListDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Schema(description = "양조장 상세 조회 응답 정보")
public class ResBreweryDto {
    @Schema(description = "양조장 식별자입니다.", example = "1")
    private final Long brewery_id;
    @Schema(description = "양조장을 소유한 회원 식별자입니다.", example = "10")
    private final Long users_id;
    @Schema(description = "양조장 회원 이메일입니다.", example = "brewery@example.com")
    private final String users_email;
    @Schema(description = "양조장 회원 연락처입니다.", example = "010-1234-5678")
    private final String users_phone;
    @Schema(description = "양조장 지역명입니다.", example = "서울")
    private final String region_type_name;
    @Schema(description = "양조장 상호명입니다.", example = "몽향양조장")
    private final String brewery_name;
    @Schema(description = "양조장 주소입니다.", example = "서울시 중구 세종대로 110")
    private final String brewery_address;
    @Schema(description = "양조장 상세 주소입니다.", example = "101호")
    private final String brewery_address_detail;
    @Schema(description = "양조장 소개글입니다.", example = "전통주 체험을 운영하는 양조장입니다.", nullable = true)
    private final String brewery_introduction;
    @Schema(description = "양조장 웹사이트 주소입니다.", example = "https://example.com", nullable = true)
    private final String brewery_website;
    @Schema(description = "양조장 등록일입니다.", example = "2026-06-01", type = "string", format = "date")
    private final LocalDate brewery_registered_at;
    @Schema(description = "정기 방문 가능 여부입니다.", example = "true")
    private final Boolean brewery_is_regular_visit;
    @Schema(description = "방문 양조장 여부입니다.", example = "true")
    private final Boolean brewery_is_visiting_brewery;
    @Setter
    @Schema(description = "양조장 이미지 목록입니다. seq=1이 대표 이미지입니다.")
    private List<ResBreweryImageDto> brewery_image_image_key; // 이미지 리스트
    @Setter
    @Schema(description = "양조장이 취급하는 온라인 판매 상품 페이지입니다. 현재 상세 조회에서는 0페이지, 페이지 크기 12 기준으로 조회합니다.")
    private Page<ResProductListDto> product_list; // 양조장이 취급하는 상품 리스트(온라인판매 안할 수도 있음)
    @Setter
    @Schema(description = "양조장에 연결된 태그 이름 목록입니다.", example = "[\"탁주\", \"체험\"]")
    private List<String> tags_name; // 양조장 주종 태그 이름 리스트
    @Setter
    @Schema(description = "양조장의 활성 체험 목록입니다.")
    private List<ResJoyDto> joy;

    private ResBreweryDto(Brewery brewery) {
        this.brewery_id = brewery.getId();
        this.users_id = brewery.getUser().getId();
        this.users_email = brewery.getUser().getEmail();
        this.users_phone = brewery.getUser().getPhone();
        this.region_type_name = brewery.getRegionType().getName();
        this.brewery_name = brewery.getBreweryName();
        this.brewery_address = brewery.getBreweryAddress();
        this.brewery_address_detail = brewery.getBreweryAddressDetail();
        this.brewery_introduction = brewery.getIntroduction();
        this.brewery_website = getBrewery_website();
        this.brewery_registered_at = brewery.getRegisteredAt();
        this.brewery_is_regular_visit = brewery.getIsRegularVisit();
        this.brewery_is_visiting_brewery = brewery.getIsVisitingBrewery();
    }

    /**
     *
     * @param brewery 지역 정보 테이블과 회원 테이블과 fetch join되었으며, 삭제 처리되지 않은 brewery 엔티티
     * @return ResBreweryDto
     */
    public static ResBreweryDto activeBreweryFrom(Brewery brewery) {
        return new ResBreweryDto(brewery);
    }

}
