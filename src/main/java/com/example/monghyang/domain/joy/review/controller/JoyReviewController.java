package com.example.monghyang.domain.joy.review.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.joy.review.dto.ReqJoyReviewDto;
import com.example.monghyang.domain.joy.review.dto.ReqUpdateJoyReviewDto;
import com.example.monghyang.domain.joy.review.dto.ResJoyReviewDto;
import com.example.monghyang.domain.joy.review.service.JoyReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/joy-review")
public class JoyReviewController {
    private final JoyReviewService joyReviewService;

    @PostMapping
    @Operation(summary = "체험 댓글형 리뷰 작성")
    public ResponseEntity<ResponseDataDto<Void>> createJoyReview(@LoginUserId Long userId, @ModelAttribute @Valid ReqJoyReviewDto dto) {
        joyReviewService.addReview(userId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 작성되었습니다."));
    }

    @PostMapping("/{joyReviewId}")
    @Operation(summary = "체험 댓글형 리뷰 수정")
    public ResponseEntity<ResponseDataDto<Void>> updateJoyReview(@LoginUserId Long userId, @PathVariable Long joyReviewId, @ModelAttribute @Valid ReqUpdateJoyReviewDto dto) {
        joyReviewService.updateReview(userId, joyReviewId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 수정되었습니다."));
    }

    @DeleteMapping("/{joyReviewId}")
    @Operation(summary = "체험 댓글형 리뷰 삭제(soft-delete)")
    public ResponseEntity<ResponseDataDto<Void>> deleteJoyReview(@LoginUserId Long userId, @PathVariable Long joyReviewId) {
        joyReviewService.deleteReview(userId, joyReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 삭제되었습니다."));
    }

    @PostMapping("/like/{joyReviewId}")
    @Operation(summary = "체험 댓글형 리뷰 좋아요 추가")
    public ResponseEntity<ResponseDataDto<Void>> likeJoyReview(@LoginUserId Long userId, @PathVariable Long joyReviewId) {
        joyReviewService.likeJoyReview(userId, joyReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("좋아요를 표시하였습니다."));
    }

    @PostMapping("/unlike/{joyReviewId}")
    @Operation(summary = "체험 댓글형 리뷰 좋아요 삭제")
    public ResponseEntity<ResponseDataDto<Void>> unlikeJoyReview(@LoginUserId Long userId, @PathVariable Long joyReviewId) {
        joyReviewService.unLikeJoyReview(userId, joyReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("좋아요를 삭제하였습니다."));
    }

    @GetMapping("/latest/by-brewery/{breweryId}/{startOffset}")
    @Operation(summary = "특정 양조장의 체험 댓글형 리뷰 최신순 조회(체험 구분 X)")
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByBreweryIdLatest(@PathVariable Long breweryId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findLatest(breweryId, startOffset)));
    }

    @GetMapping("/likes-desc/by-brewery/{breweryId}/{startOffset}")
    @Operation(summary = "특정 양조장의 체험 댓글형 리뷰 좋아요 많은 순 조회(체험 구분 X)")
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByBreweryIdLikesDesc(@PathVariable Long breweryId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findLikesDesc(breweryId, startOffset)));
    }

    @GetMapping("/star-desc/by-brewery/{breweryId}/{startOffset}")
    @Operation(summary = "특정 양조장의 체험 댓글형 리뷰 별점 높은 순 조회(체험 구분 X)")
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByBreweryIdStarDesc(@PathVariable Long breweryId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findStarDesc(breweryId, startOffset)));
    }

    @GetMapping("/latest/by-joy/{joyId}/{startOffset}")
    @Operation(summary = "특정 체험의 댓글형 리뷰 최신순 조회")
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByJoyIdLatest(@PathVariable Long joyId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findByJoyLatest(joyId, startOffset)));
    }

    @GetMapping("/likes-desc/by-joy/{joyId}/{startOffset}")
    @Operation(summary = "특정 체험의 댓글형 리뷰 좋아요 많은 순 조회")
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByJoyIdLikesDesc(@PathVariable Long joyId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findByJoyLikesDesc(joyId, startOffset)));
    }

    @GetMapping("/star-desc/by-joy/{joyId}/{startOffset}")
    @Operation(summary = "특정 체험의 댓글형 리뷰 별점 높은 순 조회")
    public ResponseEntity<ResponseDataDto<Page<ResJoyReviewDto>>> findByJoyIdStarDesc(@PathVariable Long joyId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyReviewService.findByJoyStarDesc(joyId, startOffset)));
    }
}
