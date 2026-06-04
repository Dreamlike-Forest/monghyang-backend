package com.example.monghyang.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "커뮤니티 댓글 작성 요청 정보")
public class ReqCommentDto {
    @Schema(description = "댓글을 작성할 커뮤니티 게시글 식별자입니다.", example = "1")
    private Long communityId;
    @Schema(description = "대댓글일 때 부모 댓글 식별자입니다. 일반 댓글이면 null입니다.", example = "10", nullable = true)
    private Long parentCommentId;
    @Schema(description = "댓글 본문입니다.", example = "좋은 후기 감사합니다.")
    private String content;
}
