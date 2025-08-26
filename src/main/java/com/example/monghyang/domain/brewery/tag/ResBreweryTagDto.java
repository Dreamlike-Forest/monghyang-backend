package com.example.monghyang.domain.brewery.tag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResBreweryTagDto {
    private Integer tags_id;
    private String tags_name;

    private ResBreweryTagDto(Integer tags_id, String tags_name) {
        this.tags_id = tags_id;
        this.tags_name = tags_name;
    }
    public static ResBreweryTagDto tagIdTagName(Integer tags_id, String tags_name) {
        return new ResBreweryTagDto(tags_id, tags_name);
    }
}
