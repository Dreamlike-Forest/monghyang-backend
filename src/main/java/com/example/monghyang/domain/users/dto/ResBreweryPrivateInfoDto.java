package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.brewery.entity.Brewery;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ResBreweryPrivateInfoDto {
    private final Long brewery_id;
    private final Integer region_type_id;
    private final String region_type_name;
    private final String brewery_name;
    private final String brewery_address;
    private final String brewery_address_detail;
    private final LocalDate brewery_registered_at;
    private final String brewery_business_registration_number;
    private final String brewery_depositor;
    private final String brewery_account_number;
    private final String brewery_bank_name;
    private final String brewery_introduction;
    private final String brewery_website;
    private final LocalTime brewery_start_time;
    private final LocalTime brewery_end_time;
    private final Boolean brewery_is_regular_visit;
    private final Boolean brewery_is_visiting_brewery;
    private final Boolean brewery_is_agreed_brewery;
    private final Boolean brewery_is_deleted;

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
        this.brewery_start_time = brewery.getStartTime();
        this.brewery_end_time = brewery.getEndTime();
        this.brewery_is_regular_visit = brewery.getIsRegularVisit();
        this.brewery_is_visiting_brewery = brewery.getIsVisitingBrewery();
        this.brewery_is_agreed_brewery = brewery.getIsAgreedBrewery();
        this.brewery_is_deleted = brewery.getIsDeleted();
    }
}
