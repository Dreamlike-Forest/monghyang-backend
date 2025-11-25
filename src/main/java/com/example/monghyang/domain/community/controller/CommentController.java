package com.example.monghyang.domain.community.controller;

import com.example.monghyang.domain.community.dto.ReqCommentDto;
import com.example.monghyang.domain.community.dto.ResCommentDto;
import com.example.monghyang.domain.community.service.CommentService;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 API")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 작성", description = "커뮤니티 게시글에 댓글을 작성합니다.")
    public ResponseDataDto<ResCommentDto> createComment(
            @LoginUserId Long userId,
            @ModelAttribute ReqCommentDto dto) {
        ResCommentDto result = commentService.createComment(userId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/community/{communityId}")
    @Operation(summary = "커뮤니티 게시글의 댓글 조회", description = "특정 커뮤니티 게시글의 모든 댓글을 조회합니다.")
    public ResponseDataDto<List<ResCommentDto>> getCommentsByCommunity(@PathVariable Long communityId) {
        List<ResCommentDto> result = commentService.getCommentsByCommunity(communityId);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/replies/{parentCommentId}")
    @Operation(summary = "대댓글 조회", description = "특정 댓글의 대댓글을 조회합니다.")
    public ResponseDataDto<List<ResCommentDto>> getRepliesByComment(@PathVariable Long parentCommentId) {
        List<ResCommentDto> result = commentService.getRepliesByComment(parentCommentId);
        return ResponseDataDto.contentFrom(result);
    }

    @PostMapping("/{commentId}") // put을 posy로 변환
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    public ResponseDataDto<ResCommentDto> updateComment(
            @LoginUserId Long userId,
            @PathVariable Long commentId,
            @RequestParam("content") String content) {
        ResCommentDto result = commentService.updateComment(userId, commentId, content);
        return ResponseDataDto.contentFrom(result);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public ResponseDataDto<Void> deleteComment(
            @LoginUserId Long userId,
            @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        return ResponseDataDto.success("댓글이 삭제되었습니다.");

    }
}
