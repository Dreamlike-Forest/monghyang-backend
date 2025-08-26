package com.example.monghyang.domain.tag.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResTagDto {
    private Integer id;
    private String tag_category_name;
    private String name;

    private ResTagDto(Integer id, String tag_category_name, String name) {
        this.id = id;
        this.tag_category_name = tag_category_name;
        this.name = name;
    }

    public static ResTagDto idTagCategoryNameName(Integer id, String tag_category_name, String name) {
        return new ResTagDto(id, tag_category_name, name);
    }
}
