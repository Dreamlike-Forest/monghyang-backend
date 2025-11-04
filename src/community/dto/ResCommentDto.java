package com.example.monghyang.domain.community.dto;

import com.example.monghyang.domain.community.entity.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResCommentDto {
    @JsonProperty("comment_id")
    private Long commentId;

    @JsonProperty("community_id")
    private Long communityId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("parent_comment_id")
    private Long parentCommentId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static ResCommentDto from(Comment comment) {
        return ResCommentDto.builder()
                .commentId(comment.getId())
                .communityId(comment.getCommunity().getId())
                .userId(comment.getUser().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
