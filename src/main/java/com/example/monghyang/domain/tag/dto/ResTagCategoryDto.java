package com.example.monghyang.domain.tag.dto;

import com.example.monghyang.domain.tag.entity.TagCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResTagCategoryDto {
    private Integer id;
    private String name;

    private ResTagCategoryDto(TagCategory tagCategory) {
        this.id = tagCategory.getId();
        this.name = tagCategory.getName();
    }

    public static ResTagCategoryDto tagCategoryFrom(TagCategory tagCategory) {
        return new ResTagCategoryDto(tagCategory);
    }
}
