package com.example.monghyang.domain.brewery.main.dto;

import com.example.monghyang.domain.brewery.joy.dto.ResJoyDto;
import com.example.monghyang.domain.brewery.main.entity.Brewery;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ResBreweryDto {
    private Long brewery_id;
    private Long users_id;
    private String users_email;
    private String users_phone;
    private String region_type_name;
    private String brewery_name;
    private String brewery_address;
    private String brewery_address_detail;
    private String brewery_introduction;
    private String brewery_website;
    private LocalDate brewery_registered_at;
    private Boolean brewery_is_regular_visit;
    private Boolean brewery_is_visiting_brewery;
    private List<ResBreweryImageDto> brewery_image_image_key; // 이미지 리스트
    private List<String> tags_name; // 양조장 주종 태그 이름 리스트
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

    public void setTags_name(List<String> tags_name) {
        this.tags_name = tags_name;
    }

    public void setBrewery_image_image_key(List<ResBreweryImageDto> brewery_image_image_key) {
        this.brewery_image_image_key = brewery_image_image_key;
    }

    public void setJoy(List<ResJoyDto> joy) {
        this.joy = joy;
    }
}
