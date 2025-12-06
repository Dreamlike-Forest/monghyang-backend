package com.example.monghyang.domain.joy.review.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.joy.review.dto.ReqJoyReviewDto;
import com.example.monghyang.domain.joy.review.dto.ReqUpdateJoyReviewDto;
import com.example.monghyang.domain.joy.review.service.JoyReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
