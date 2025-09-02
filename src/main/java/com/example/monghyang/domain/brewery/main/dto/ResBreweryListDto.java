package com.example.monghyang.domain.brewery.main.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ResBreweryListDto {
    private final Long brewery_id;
    private final String brewery_name;
    private final String region_type_name;
    private final String brewery_introduction;
    private final Integer brewery_joy_min_price;
    private final Integer brewery_joy_count;
    private final String image_key;
    @Setter
    private List<String> tag_name;
}
