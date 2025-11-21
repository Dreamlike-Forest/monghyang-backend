package com.example.monghyang.domain.community.controller;

import com.example.monghyang.domain.community.dto.PageResponseDto;
import com.example.monghyang.domain.community.dto.ReqCommunityDto;
import com.example.monghyang.domain.community.dto.ResCommunityDto;
import com.example.monghyang.domain.community.dto.ResCommunityListDto;
import com.example.monghyang.domain.community.service.CommunityService;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "커뮤니티 게시글 작성", description = "새로운 커뮤니티 게시글을 작성합니다.")
    public ResponseDataDto<ResCommunityDto> createCommunity(
            @LoginUserId Long userId,
            @ModelAttribute ReqCommunityDto dto) {
        ResCommunityDto result = communityService.createCommunity(userId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping
    @Operation(summary = "전체 커뮤니티 게시글 조회", description = "모든 커뮤니티 게시글을 조회합니다.")
    public ResponseDataDto<List<ResCommunityListDto>> getAllCommunities() {
        List<ResCommunityListDto> result = communityService.getAllCommunities();
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/page")
    @Operation(summary = "전체 커뮤니티 게시글 조회 (페이징)", description = "모든 커뮤니티 게시글을 페이징하여 조회합니다. (12개씩)")
    public ResponseDataDto<PageResponseDto<ResCommunityListDto>> getAllCommunitiesWithPaging(
            @RequestParam(defaultValue = "0") int page) {
        PageResponseDto<ResCommunityListDto> result = communityService.getAllCommunitiesWithPaging(page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 커뮤니티 게시글 조회", description = "특정 카테고리의 커뮤니티 게시글을 조회합니다.")
    public ResponseDataDto<List<ResCommunityListDto>> getCommunitiesByCategory(@PathVariable String category) {
        List<ResCommunityListDto> result = communityService.getCommunitiesByCategory(category);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/category/{category}/page")
    @Operation(summary = "카테고리별 커뮤니티 게시글 조회 (페이징)", description = "특정 카테고리의 커뮤니티 게시글을 페이징하여 조회합니다. (12개씩)")
    public ResponseDataDto<PageResponseDto<ResCommunityListDto>> getCommunitiesByCategoryWithPaging(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page) {
        PageResponseDto<ResCommunityListDto> result = communityService.getCommunitiesByCategoryWithPaging(category, page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 커뮤니티 게시글 조회", description = "특정 사용자의 커뮤니티 게시글을 조회합니다.")
    public ResponseDataDto<List<ResCommunityListDto>> getCommunitiesByUser(@PathVariable Long userId) {
        List<ResCommunityListDto> result = communityService.getCommunitiesByUser(userId);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/user/{userId}/page")
    @Operation(summary = "사용자별 커뮤니티 게시글 조회 (페이징)", description = "특정 사용자의 커뮤니티 게시글을 페이징하여 조회합니다. (12개씩)")
    public ResponseDataDto<PageResponseDto<ResCommunityListDto>> getCommunitiesByUserWithPaging(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page) {
        PageResponseDto<ResCommunityListDto> result = communityService.getCommunitiesByUserWithPaging(userId, page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{communityId}")
    @Operation(summary = "커뮤니티 게시글 상세 조회", description = "특정 커뮤니티 게시글의 상세 정보를 조회합니다.")
    public ResponseDataDto<ResCommunityDto> getCommunityById(@PathVariable Long communityId) {
        ResCommunityDto result = communityService.getCommunityById(communityId);
        return ResponseDataDto.contentFrom(result);
    }

    @PutMapping("/{communityId}")
    @Operation(summary = "커뮤니티 게시글 수정", description = "커뮤니티 게시글을 수정합니다.")
    public ResponseDataDto<ResCommunityDto> updateCommunity(
            @LoginUserId Long userId,
            @PathVariable Long communityId,
            @ModelAttribute ReqCommunityDto dto) {
        ResCommunityDto result = communityService.updateCommunity(userId, communityId, dto);
        return ResponseDataDto.contentFrom(result);
    }

    @DeleteMapping("/{communityId}")
    @Operation(summary = "커뮤니티 게시글 삭제", description = "커뮤니티 게시글을 삭제합니다.")
    public ResponseDataDto<Void> deleteCommunity(
            @LoginUserId Long userId,
            @PathVariable Long communityId) {
        communityService.deleteCommunity(userId, communityId);
        return ResponseDataDto.success("커뮤니티 게시글이 삭제되었습니다.");
    }

    @PostMapping("/{communityId}/like")
    @Operation(summary = "커뮤니티 게시글 좋아요", description = "커뮤니티 게시글에 좋아요를 추가합니다.")
    public ResponseDataDto<Void> likeCommunity(@PathVariable Long communityId) {
        communityService.likeCommunity(communityId);
        return ResponseDataDto.success("좋아요가 추가되었습니다.");
    }

    @DeleteMapping("/{communityId}/like")
    @Operation(summary = "커뮤니티 게시글 좋아요 취소", description = "커뮤니티 게시글의 좋아요를 취소합니다.")
    public ResponseDataDto<Void> unlikeCommunity(@PathVariable Long communityId) {
        communityService.unlikeCommunity(communityId);
        return ResponseDataDto.success("좋아요가 취소되었습니다.");
    }
}
