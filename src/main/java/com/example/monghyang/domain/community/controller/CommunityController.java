package com.example.monghyang.domain.community.controller;

import com.example.monghyang.domain.community.dto.PageResponseDto;
import com.example.monghyang.domain.community.dto.ReqCommunityDto;
import com.example.monghyang.domain.community.dto.ResCommunityDto;
import com.example.monghyang.domain.community.dto.ResCommunityListDto;
import com.example.monghyang.domain.community.service.CommunityService;
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
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "Community", description = "커뮤니티 API")
public class CommunityController {
    private final CommunityService communityService;

    @PostMapping
    @Operation(summary = "커뮤니티 게시글 작성", description = "새로운 커뮤니티 게시글을 작성합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 게시글 작성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "게시글 입력값 또는 이미지 요청이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResCommunityDto> createCommunity(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @ModelAttribute ReqCommunityDto dto) {
        ResCommunityDto result = communityService.createCommunity(userId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping
    @Operation(summary = "전체 커뮤니티 게시글 조회", description = "모든 커뮤니티 게시글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 커뮤니티 게시글 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class)))
    })
    public ResponseDataDto<List<ResCommunityListDto>> getAllCommunities() {
        List<ResCommunityListDto> result = communityService.getAllCommunities();
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/page/{page}")
    @Operation(summary = "전체 커뮤니티 게시글 조회 (페이징)", description = "모든 커뮤니티 게시글을 페이징하여 조회합니다. (12개씩)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 커뮤니티 게시글 페이징 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<PageResponseDto<ResCommunityListDto>> getAllCommunitiesWithPaging(
            @PathVariable Integer page) {
        PageResponseDto<ResCommunityListDto> result = communityService.getAllCommunitiesWithPaging(page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 커뮤니티 게시글 조회", description = "특정 카테고리의 커뮤니티 게시글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리별 커뮤니티 게시글 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class)))
    })
    public ResponseDataDto<List<ResCommunityListDto>> getCommunitiesByCategory(@PathVariable String category) {
        List<ResCommunityListDto> result = communityService.getCommunitiesByCategory(category);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/category/{category}/page/{page}")
    @Operation(summary = "카테고리별 커뮤니티 게시글 조회 (페이징)", description = "특정 카테고리의 커뮤니티 게시글을 페이징하여 조회합니다. (12개씩)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리별 커뮤니티 게시글 페이징 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "카테고리 또는 페이지 번호가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<PageResponseDto<ResCommunityListDto>> getCommunitiesByCategoryWithPaging(
            @PathVariable String category,
            @PathVariable Integer page) {
        PageResponseDto<ResCommunityListDto> result = communityService.getCommunitiesByCategoryWithPaging(category, page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 커뮤니티 게시글 조회", description = "특정 사용자의 커뮤니티 게시글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자별 커뮤니티 게시글 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<List<ResCommunityListDto>> getCommunitiesByUser(@PathVariable Long userId) {
        List<ResCommunityListDto> result = communityService.getCommunitiesByUser(userId);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/user/{userId}/page/{page}")
    @Operation(summary = "사용자별 커뮤니티 게시글 조회 (페이징)", description = "특정 사용자의 커뮤니티 게시글을 페이징하여 조회합니다. (12개씩)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자별 커뮤니티 게시글 페이징 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<PageResponseDto<ResCommunityListDto>> getCommunitiesByUserWithPaging(
            @PathVariable Long userId,
            @PathVariable Integer page) {
        PageResponseDto<ResCommunityListDto> result = communityService.getCommunitiesByUserWithPaging(userId, page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{communityId}") // 비로그인자도 조회 가능
    @Operation(summary = "커뮤니티 게시글 상세 조회", description = "특정 커뮤니티 게시글의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 게시글 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResCommunityDto> getCommunityById(
            @PathVariable Long communityId,
            @Parameter(hidden = true) @LoginUserId(required = false) Long userId) {
        ResCommunityDto result = communityService.getCommunityById(communityId, userId);
        return ResponseDataDto.contentFrom(result);
    }

    @PostMapping("/{communityId}") // put을 post로 변환
    @Operation(summary = "커뮤니티 게시글 수정", description = "커뮤니티 게시글을 수정합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 게시글 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "게시글 수정 입력값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "게시글 수정 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResCommunityDto> updateCommunity(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long communityId,
            @ModelAttribute ReqCommunityDto dto) {
        ResCommunityDto result = communityService.updateCommunity(userId, communityId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @DeleteMapping("/{communityId}")
    @Operation(summary = "커뮤니티 게시글 삭제", description = "커뮤니티 게시글을 삭제합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 게시글 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "게시글 삭제 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Void> deleteCommunity(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long communityId) {
        communityService.deleteCommunity(userId, communityId);
        return ResponseDataDto.success("커뮤니티 게시글이 삭제되었습니다.");
    }

    @PostMapping("/{communityId}/like")
    @Operation(summary = "커뮤니티 게시글 좋아요", description = "커뮤니티 게시글에 좋아요를 추가합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 게시글 좋아요 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "이미 좋아요를 누른 게시글",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Void> likeCommunity(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long communityId) {
        communityService.likeCommunity(userId, communityId);
        return ResponseDataDto.success("좋아요가 추가되었습니다.");
    }

    @DeleteMapping("/{communityId}/like")
    @Operation(summary = "커뮤니티 게시글 좋아요 취소", description = "커뮤니티 게시글의 좋아요를 취소합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 게시글 좋아요 취소 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글 또는 좋아요 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Void> unlikeCommunity(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long communityId) {
        communityService.unlikeCommunity(userId, communityId);
        return ResponseDataDto.success("좋아요가 취소되었습니다.");
    }
}
