package com.example.monghyang.domain.product.review;

import com.example.monghyang.domain.global.annotation.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.product.review.dto.ReqProductReviewDto;
import com.example.monghyang.domain.product.review.dto.ResProductReviewListDto;
import com.example.monghyang.domain.product.review.dto.UpdateProductReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-review")
@Tag(name = "상품 리뷰 API")
public class ProductReviewController {
    private final ProductReviewService productReviewService;
    @Autowired
    public ProductReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    // 리뷰 추가
    @PostMapping
    @Operation(summary = "리뷰 작성")
    public ResponseEntity<ResponseDataDto<Void>> createReview(@LoginUserId Long userId, @Valid @ModelAttribute ReqProductReviewDto reqProductReviewDto) {
        productReviewService.createReview(userId, reqProductReviewDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 추가되었습니다."));
    }

    // 리뷰 수정
    @PostMapping("/update")
    @Operation(summary = "리뷰 수정")
    public ResponseEntity<ResponseDataDto<Void>> updateReview(@LoginUserId Long userId, @Valid @ModelAttribute UpdateProductReviewDto updateProductReviewDto) {
        productReviewService.updateReview(userId, updateProductReviewDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 수정되었습니다."));
    }

    // 리뷰 삭제
    @DeleteMapping("/{productReviewId}")
    @Operation(summary = "리뷰 삭제")
    public ResponseEntity<ResponseDataDto<Void>> deleteReview(@LoginUserId Long userId, @PathVariable Long productReviewId) {
        productReviewService.deleteReview(userId, productReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 삭제되었습니다."));
    }

    // 리뷰 복구
    @PostMapping("/restore/{productReviewId}")
    @Operation(summary = "리뷰 복구")
    public ResponseEntity<ResponseDataDto<Void>> restoreReview(@LoginUserId Long userId, @PathVariable Long productReviewId) {
        productReviewService.restoreReview(userId, productReviewId);
        return ResponseEntity.ok().body(ResponseDataDto.success("리뷰가 복구되었습니다."));
    }

    // 특정 상품의 리뷰 최신순 조회(페이징)
    @GetMapping("/latest/{productId}/{startOffset}")
    public ResponseEntity<ResponseDataDto<Page<ResProductReviewListDto>>> getReviewLatest(@PathVariable Long productId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(productReviewService.getReviewByProductIdLatest(productId, startOffset)));
    }
}
