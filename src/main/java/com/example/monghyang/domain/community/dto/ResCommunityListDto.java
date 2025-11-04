package com.example.monghyang.domain.community.dto;

import com.example.monghyang.domain.community.entity.Community;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResCommunityListDto {
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

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("view_count")
    private Integer viewCount;

    @JsonProperty("likes")
    private Integer likes;

    @JsonProperty("comments")
    private Integer comments;

    public static ResCommunityListDto from(Community community) {
        return ResCommunityListDto.builder()
                .communityId(community.getId())
                .userId(community.getUser().getId())
                .title(community.getTitle())
                .category(community.getCategory())
                .subCategory(community.getSubCategory())
                .createdAt(community.getCreatedAt())
                .viewCount(community.getViewCount())
                .likes(community.getLikes())
                .comments(community.getComments())
                .build();
    }
}
