package com.example.monghyang.domain.community.controller;

import com.example.monghyang.domain.community.dto.ReqCommentDto;
import com.example.monghyang.domain.community.dto.ResCommentDto;
import com.example.monghyang.domain.community.service.CommentService;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(summary = "댓글 작성", description = "커뮤니티 게시글에 댓글을 작성합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 작성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResCommentDto> createComment(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @ModelAttribute ReqCommentDto dto) {
        ResCommentDto result = commentService.createComment(userId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/community/{communityId}")
    @Operation(summary = "커뮤니티 게시글의 댓글 조회", description = "특정 커뮤니티 게시글의 모든 댓글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 게시글 댓글 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<List<ResCommentDto>> getCommentsByCommunity(@PathVariable Long communityId) {
        List<ResCommentDto> result = commentService.getCommentsByCommunity(communityId);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/replies/{parentCommentId}")
    @Operation(summary = "대댓글 조회", description = "특정 댓글의 대댓글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대댓글 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "부모 댓글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<List<ResCommentDto>> getRepliesByComment(@PathVariable Long parentCommentId) {
        List<ResCommentDto> result = commentService.getRepliesByComment(parentCommentId);
        return ResponseDataDto.contentFrom(result);
    }

    @PostMapping("/{commentId}") // put을 posy로 변환
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "댓글 내용이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "댓글 수정 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResCommentDto> updateComment(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long commentId,
            @RequestParam("content") String content) {
        ResCommentDto result = commentService.updateComment(userId, commentId, content);
        return ResponseDataDto.contentFrom(result);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "댓글 삭제 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Void> deleteComment(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        return ResponseDataDto.success("댓글이 삭제되었습니다.");

    }
}
