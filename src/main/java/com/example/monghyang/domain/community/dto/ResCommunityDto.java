package com.example.monghyang.domain.community.dto;

import com.example.monghyang.domain.community.entity.Community;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResCommunityDto {
    @JsonProperty("community_id")
    private Long communityId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("category")
    private String category;

    @JsonProperty("sub_category")
    private String subCategory;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("brewery_name")
    private String breweryName;

    @JsonProperty("star")
    private Double star;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("view_count")
    private Integer viewCount;

    @JsonProperty("likes")
    private Integer likes;

    @JsonProperty("comments")
    private Integer comments;

    public static ResCommunityDto from(Community community) {
        return ResCommunityDto.builder()
                .communityId(community.getId())
                .userId(community.getUser().getId())
                .title(community.getTitle())
                .category(community.getCategory())
                .subCategory(community.getSubCategory())
                .productName(community.getProductName())
                .breweryName(community.getBreweryName())
                .star(community.getStar())
                .detail(community.getDetail())
                .tags(community.getTags())
                .createdAt(community.getCreatedAt())
                .viewCount(community.getViewCount())
                .likes(community.getLikes())
                .comments(community.getComments())
                .build();
    }
}
