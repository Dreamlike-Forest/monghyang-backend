package com.example.monghyang.domain.tag.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = Boolean.FALSE;

    private TagCategory(String name) {
        this.name = name;
    }

    public static TagCategory categoryNameFrom(@NonNull String name) {
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
