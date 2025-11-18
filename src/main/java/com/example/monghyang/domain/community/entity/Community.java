package com.example.monghyang.domain.community.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMUNITY_ID")
    private Long id;

    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    @Column(nullable = false)
    private String title;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(length = 50, nullable = false)
    private String subCategory;

    @Column(length = 100)
    private String productName;

    @Column(length = 100)
    private String breweryName;

    private Double star;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String detail;

    private String tags;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private Integer comments = 0;

    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Builder
    public Community(Users user, String title, String category, String subCategory,
                     String productName, String breweryName, Double star, String detail, String tags) {
        this.user = user;
        this.title = title;
        this.category = category;
        this.subCategory = subCategory;
        this.productName = productName;
        this.breweryName = breweryName;
        this.star = star;
        this.detail = detail;
        this.tags = tags;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateCategory(String category) {
        this.category = category;
    }

    public void updateSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public void updateProductName(String productName) {
        this.productName = productName;
    }

    public void updateBreweryName(String breweryName) {
        this.breweryName = breweryName;
    }

    public void updateStar(Double star) {
        this.star = star;
    }

    public void updateDetail(String detail) {
        this.detail = detail;
    }

    public void updateTags(String tags) {
        this.tags = tags;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikes() {
        this.likes++;
    }

    public void decreaseLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    public void increaseComments() {
        this.comments++;
    }

    public void decreaseComments() {
        if (this.comments > 0) {
            this.comments--;
        }
    }

    public void setDeleted() {
        this.isDeleted = Boolean.TRUE;
    }

    public void unSetDeleted() {
        this.isDeleted = Boolean.FALSE;
    }
}
