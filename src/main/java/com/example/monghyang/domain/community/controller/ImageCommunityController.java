package com.example.monghyang.domain.community.controller;

import com.example.monghyang.domain.community.dto.ResImageCommunityDto;
import com.example.monghyang.domain.community.service.ImageCommunityService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/community/image")
@RequiredArgsConstructor
@Tag(name = "Community Image", description = "커뮤니티 이미지 API")
public class ImageCommunityController {
    private final ImageCommunityService imageCommunityService;

    @PostMapping("/{communityId}")
    @Operation(summary = "커뮤니티 이미지 업로드", description = "커뮤니티 게시글에 이미지를 업로드합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 이미지 업로드 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "이미지 형식, 크기 또는 순서가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResImageCommunityDto> uploadImage(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long communityId,
            @RequestParam Integer imageNum,
            @RequestParam("file") MultipartFile file) {
        ResImageCommunityDto result = imageCommunityService.uploadImage(userId, communityId, imageNum, file);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{communityId}")
    @Operation(summary = "커뮤니티 이미지 조회", description = "특정 커뮤니티 게시글의 모든 이미지를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 이미지 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 게시글이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<List<ResImageCommunityDto>> getImagesByCommunity(@PathVariable Long communityId) {
        List<ResImageCommunityDto> result = imageCommunityService.getImagesByCommunity(communityId);
        return ResponseDataDto.contentFrom(result);
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "커뮤니티 이미지 삭제", description = "커뮤니티 이미지를 삭제합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "커뮤니티 이미지 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "커뮤니티 이미지 삭제 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "커뮤니티 이미지가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Void> deleteImage(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long imageId) {
        imageCommunityService.deleteImage(userId, imageId);
        return ResponseDataDto.success("이미지가 삭제되었습니다.");
    }
}
