package com.example.monghyang.domain.community.controller;

import com.example.monghyang.domain.community.dto.ResImageCommunityDto;
import com.example.monghyang.domain.community.service.ImageCommunityService;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/community/image")
@RequiredArgsConstructor
@Tag(name = "Community Image", description = "커뮤니티 이미지 API")
public class ImageCommunityController {
    private final ImageCommunityService imageCommunityService;

    @PostMapping("/{communityId}")
    @Operation(summary = "커뮤니티 이미지 업로드", description = "커뮤니티 게시글에 이미지를 업로드합니다.")
    public ResponseDataDto<ResImageCommunityDto> uploadImage(
            @LoginUserId Long userId,
            @PathVariable Long communityId,
            @RequestParam Integer imageNum,
            @RequestParam("file") MultipartFile file) {
        ResImageCommunityDto result = imageCommunityService.uploadImage(userId, communityId, imageNum, file);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{communityId}")
    @Operation(summary = "커뮤니티 이미지 조회", description = "특정 커뮤니티 게시글의 모든 이미지를 조회합니다.")
    public ResponseDataDto<List<ResImageCommunityDto>> getImagesByCommunity(@PathVariable Long communityId) {
        List<ResImageCommunityDto> result = imageCommunityService.getImagesByCommunity(communityId);
        return ResponseDataDto.contentFrom(result);
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "커뮤니티 이미지 삭제", description = "커뮤니티 이미지를 삭제합니다.")
    public ResponseDataDto<Void> deleteImage(
            @LoginUserId Long userId,
            @PathVariable Long imageId) {
        imageCommunityService.deleteImage(userId, imageId);
        return ResponseDataDto.success("이미지가 삭제되었습니다.");
    }
}
