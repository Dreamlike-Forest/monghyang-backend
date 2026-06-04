package com.example.monghyang.domain.product.review;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.product.review.dto.ReqProductReviewDto;
import com.example.monghyang.domain.product.review.dto.ResProductReviewListDto;
import com.example.monghyang.domain.product.review.dto.UpdateProductReviewDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-review")
@Tag(name = "상품 리뷰 API")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    // 리뷰 추가
    @PostMapping
    @Operation(summary = "리뷰 작성", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 리뷰 작성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "리뷰 내용 또는 별점 형식이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 또는 상품 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> createReview(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute ReqProductReviewDto reqProductReviewDto) {
        productReviewService.createReview(userId, reqProductReviewDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 추가되었습니다."));
    }

    // 리뷰 수정
    @PostMapping("/update")
    @Operation(summary = "리뷰 수정", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 리뷰 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "리뷰 내용 또는 별점 형식이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 리뷰가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateReview(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute UpdateProductReviewDto updateProductReviewDto) {
        productReviewService.updateReview(userId, updateProductReviewDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 수정되었습니다."));
    }

    // 리뷰 삭제
    @DeleteMapping("/{productReviewId}")
    @Operation(summary = "리뷰 삭제", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 리뷰 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 리뷰가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteReview(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long productReviewId) {
        productReviewService.deleteReview(userId, productReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 삭제되었습니다."));
    }

    // 리뷰 복구
    @PostMapping("/restore/{productReviewId}")
    @Operation(summary = "리뷰 복구", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 리뷰 복구 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 리뷰가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> restoreReview(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long productReviewId) {
        productReviewService.restoreReview(userId, productReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 복구되었습니다."));
    }

    // 특정 상품의 리뷰 최신순 조회(페이징)
    @GetMapping("/latest/{productId}/{startOffset}")
    @Operation(summary = "특정 상품의 리뷰 최신순 조회", description = "상품 식별자 기준으로 리뷰를 최신순으로 페이지 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 리뷰 최신순 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "페이지 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "상품 또는 리뷰 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResProductReviewListDto>>> getReviewLatest(@PathVariable Long productId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productReviewService.getReviewByProductIdLatest(productId, startOffset)));
    }
}
