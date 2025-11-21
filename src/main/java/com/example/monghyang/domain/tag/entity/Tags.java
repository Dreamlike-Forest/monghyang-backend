package com.example.monghyang.domain.tag.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tags {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "TAG_CATEGORY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TagCategory category;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = Boolean.FALSE;

    private Tags(TagCategory category, String name) {
        this.category = category;
        this.name = name;
    }

    public static Tags categoryNameOf(@NonNull TagCategory category, @NonNull String name) {
        return new Tags(category, name);
    }


    public void setDeleted() {
        isDeleted = Boolean.TRUE;
    }

    public void unSetDeleted() {
        isDeleted = Boolean.FALSE;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
