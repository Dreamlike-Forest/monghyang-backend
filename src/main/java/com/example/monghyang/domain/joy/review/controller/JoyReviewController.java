package com.example.monghyang.domain.joy.review.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.joy.review.dto.ReqJoyReviewDto;
import com.example.monghyang.domain.joy.review.dto.ReqUpdateJoyReviewDto;
import com.example.monghyang.domain.joy.review.dto.ResJoyReviewDto;
import com.example.monghyang.domain.joy.review.service.JoyReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/joy-review")
@Tag(name = "체험 리뷰 API", description = "체험 댓글형 리뷰 작성, 수정, 삭제, 좋아요 및 조회 API")
public class JoyReviewController {
    private final JoyReviewService joyReviewService;

    @PostMapping
    @Operation(summary = "체험 댓글형 리뷰 작성", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 리뷰 작성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "별점 형식 또는 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "체험 예약 결제 후 체험한 사용자가 아니어서 리뷰를 작성할 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원, 체험 또는 예약 내역이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> createJoyReview(@Parameter(hidden = true) @LoginUserId Long userId, @ModelAttribute @Valid ReqJoyReviewDto dto) {
        joyReviewService.addReview(userId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 작성되었습니다."));
    }

    @PostMapping("/{joyReviewId}")
    @Operation(summary = "체험 댓글형 리뷰 수정", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 리뷰 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "별점 형식 또는 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 체험 리뷰가 아닌 수정 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 리뷰가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateJoyReview(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long joyReviewId, @ModelAttribute @Valid ReqUpdateJoyReviewDto dto) {
        joyReviewService.updateReview(userId, joyReviewId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 수정되었습니다."));
    }

    @DeleteMapping("/{joyReviewId}")
    @Operation(summary = "체험 댓글형 리뷰 삭제(soft-delete)", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 리뷰 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 체험 리뷰가 아닌 삭제 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 리뷰가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteJoyReview(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long joyReviewId) {
        joyReviewService.deleteReview(userId, joyReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 삭제되었습니다."));
    }

    @PostMapping("/like/{joyReviewId}")
    @Operation(summary = "체험 댓글형 리뷰 좋아요 추가", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 리뷰 좋아요 추가 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 리뷰가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "좋아요 추가 처리 중 서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> likeJoyReview(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long joyReviewId) {
        joyReviewService.likeJoyReview(userId, joyReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("좋아요를 표시하였습니다."));
    }

    @DeleteMapping("/unlike/{joyReviewId}")
    @Operation(summary = "체험 댓글형 리뷰 좋아요 삭제", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 리뷰 좋아요 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 리뷰가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "좋아요 삭제 처리 중 서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> unlikeJoyReview(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long joyReviewId) {
        joyReviewService.unLikeJoyReview(userId, joyReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("좋아요를 삭제하였습니다."));
    }

    @GetMapping("/latest/by-brewery/{breweryId}/{startOffset}")
    @Operation(summary = "특정 양조장의 체험 댓글형 리뷰 최신순 조회(체험 구분 X)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 기준 체험 리뷰 최신순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 리뷰 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByBreweryIdLatest(@PathVariable Long breweryId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findLatest(breweryId, startOffset)));
    }

    @GetMapping("/likes-desc/by-brewery/{breweryId}/{startOffset}")
    @Operation(summary = "특정 양조장의 체험 댓글형 리뷰 좋아요 많은 순 조회(체험 구분 X)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 기준 체험 리뷰 좋아요 많은 순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 리뷰 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByBreweryIdLikesDesc(@PathVariable Long breweryId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findLikesDesc(breweryId, startOffset)));
    }

    @GetMapping("/star-desc/by-brewery/{breweryId}/{startOffset}")
    @Operation(summary = "특정 양조장의 체험 댓글형 리뷰 별점 높은 순 조회(체험 구분 X)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 기준 체험 리뷰 별점 높은 순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 리뷰 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByBreweryIdStarDesc(@PathVariable Long breweryId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findStarDesc(breweryId, startOffset)));
    }

    @GetMapping("/latest/by-joy/{joyId}/{startOffset}")
    @Operation(summary = "특정 체험의 댓글형 리뷰 최신순 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 기준 리뷰 최신순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 또는 리뷰 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByJoyIdLatest(@PathVariable Long joyId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findByJoyLatest(joyId, startOffset)));
    }

    @GetMapping("/likes-desc/by-joy/{joyId}/{startOffset}")
    @Operation(summary = "특정 체험의 댓글형 리뷰 좋아요 많은 순 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 기준 리뷰 좋아요 많은 순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 또는 리뷰 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByJoyIdLikesDesc(@PathVariable Long joyId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findByJoyLikesDesc(joyId, startOffset)));
    }

    @GetMapping("/star-desc/by-joy/{joyId}/{startOffset}")
    @Operation(summary = "특정 체험의 댓글형 리뷰 별점 높은 순 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 기준 리뷰 별점 높은 순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 또는 리뷰 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByJoyIdStarDesc(@PathVariable Long joyId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findByJoyStarDesc(joyId, startOffset)));
    }
}
