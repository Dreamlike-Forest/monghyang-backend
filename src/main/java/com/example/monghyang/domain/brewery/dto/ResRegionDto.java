package com.example.monghyang.domain.brewery.dto;

import com.example.monghyang.domain.brewery.entity.RegionType;
import lombok.Getter;

@Getter
public class ResRegionDto {
    private final Integer region_type_id;
    private final String region_type_name;
    public ResRegionDto(RegionType regionType){
        this.region_type_id = regionType.getId();
        this.region_type_name = regionType.getName();
    }
}
