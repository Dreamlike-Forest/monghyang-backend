package com.example.monghyang.domain.tag.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResTagDto {
    private Integer tags_id;
    private String tag_category_name;
    private String tags_name;

    private ResTagDto(Integer tags_id, String tag_category_name, String tags_name) {
        this.tags_id = tags_id;
        this.tag_category_name = tag_category_name;
        this.tags_name = tags_name;
    }

    public static ResTagDto idTagCategoryNameName(Integer tags_id, String tag_category_name, String tags_name) {
        return new ResTagDto(tags_id, tag_category_name, tags_name);
    }
}
