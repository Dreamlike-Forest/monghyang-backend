package com.example.monghyang.domain.tag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagCategory {
    @Id @GeneratedValue
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = Boolean.FALSE;

    private TagCategory(String name) {
        this.name = name;
    }

    public static TagCategory categoryNameFrom(String name) {
        return new TagCategory(name);
    }

    public void setDeleted() {
        isDeleted = Boolean.TRUE;
    }

    public void unSetDeleted() {
        isDeleted = Boolean.FALSE;
    }

    public void updateCategoryName(String name) {
        this.name = name;
    }
}
