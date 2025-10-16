package com.example.monghyang.domain.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCommentDto {
    private Long communityId;
    private Long parentCommentId;
    private String content;
}
