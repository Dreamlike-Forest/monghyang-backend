package com.example.monghyang.domain.tag.dto;

import lombok.Getter;

@Getter
public class ResTagListDto {
    private final Integer tags_id;
    private final String tags_name;

    private ResTagListDto(Integer tags_id, String tags_name) {
        this.tags_id = tags_id;
        this.tags_name = tags_name;
    }
    public static ResTagListDto tagIdTagName(Integer tags_id, String tags_name) {
        return new ResTagListDto(tags_id, tags_name);
    }
}
