package com.example.monghyang.domain.community.controller;

import com.example.monghyang.domain.community.dto.PageResponseDto;
import com.example.monghyang.domain.community.dto.ResFollowCountDto;
import com.example.monghyang.domain.community.dto.ResFollowDto;
import com.example.monghyang.domain.community.service.FollowService;
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
@RequestMapping("/api/follow")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 API")
public class FollowController {
    private final FollowService followService;

    @PostMapping("/{userId}")
    @Operation(summary = "팔로우", description = "특정 사용자를 팔로우합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로우 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "팔로우 대상 회원이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "이미 팔로우한 사용자",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Void> follow(
            @Parameter(hidden = true) @LoginUserId Long currentUserId,
            @PathVariable Long userId) {
        followService.follow(currentUserId, userId);
        return ResponseDataDto.success("팔로우했습니다.");
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "언팔로우", description = "특정 사용자를 언팔로우합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "언팔로우 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "팔로우 대상 회원 또는 팔로우 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Void> unfollow(
            @Parameter(hidden = true) @LoginUserId Long currentUserId,
            @PathVariable Long userId) {
        followService.unfollow(currentUserId, userId);
        return ResponseDataDto.success("언팔로우했습니다.");
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자의 팔로워 목록을 조회합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<List<ResFollowDto>> getFollowers(
            @PathVariable Long userId,
            @Parameter(hidden = true) @LoginUserId Long currentUserId) {
        List<ResFollowDto> result = followService.getFollowers(userId, currentUserId);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{userId}/followers/page/{page}")
    @Operation(summary = "팔로워 목록 조회 (페이징)", description = "특정 사용자의 팔로워 목록을 페이징하여 조회합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로워 목록 페이징 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<PageResponseDto<ResFollowDto>> getFollowersWithPaging(
            @PathVariable Long userId,
            @PathVariable Integer page,
            @Parameter(hidden = true) @LoginUserId Long currentUserId) {
        PageResponseDto<ResFollowDto> result = followService.getFollowersWithPaging(userId, currentUserId, page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{userId}/followings")
    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하는 사람들의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<List<ResFollowDto>> getFollowings(@PathVariable Long userId) {
        List<ResFollowDto> result = followService.getFollowings(userId);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{userId}/followings/page/{page}")
    @Operation(summary = "팔로잉 목록 조회 (페이징)", description = "특정 사용자가 팔로우하는 사람들의 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로잉 목록 페이징 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<PageResponseDto<ResFollowDto>> getFollowingsWithPaging(
            @PathVariable Long userId,
            @PathVariable Integer page) {
        PageResponseDto<ResFollowDto> result = followService.getFollowingsWithPaging(userId, page);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{userId}/count")
    @Operation(summary = "팔로우 카운트 조회", description = "특정 사용자의 팔로워/팔로잉 수와 팔로우 상태를 조회합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로우 카운트 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<ResFollowCountDto> getFollowCount(
            @PathVariable Long userId,
            @Parameter(hidden = true) @LoginUserId Long currentUserId) {
        ResFollowCountDto result = followService.getFollowCount(userId, currentUserId);
        return ResponseDataDto.contentFrom(result);
    }

    @GetMapping("/{userId}/status")
    @Operation(summary = "팔로우 상태 확인", description = "현재 로그인한 사용자가 특정 사용자를 팔로우하는지 확인합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로우 상태 확인 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseDataDto<Boolean> checkFollowStatus(
            @PathVariable Long userId,
            @Parameter(hidden = true) @LoginUserId Long currentUserId) {
        boolean isFollowing = followService.isFollowing(currentUserId, userId);
        return ResponseDataDto.contentFrom(isFollowing);
    }
}
