package com.example.monghyang.domain.product.review;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.product.repository.ProductRepository;
import com.example.monghyang.domain.product.review.dto.ReqProductReviewDto;
import com.example.monghyang.domain.product.review.dto.ResProductReviewListDto;
import com.example.monghyang.domain.product.review.dto.UpdateProductReviewDto;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductReviewService {
    @Getter
    private final int PRODUCT_REVIEW_PAGE_SIZE = 12;
    private final ProductReviewRepository productReviewRepository;
    private final UsersRepository usersRepository;
    private final ProductRepository productRepository;

    // 리뷰 작성
    public void createReview(Long userId, ReqProductReviewDto reqProductReviewDto) {
        Users user = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        Product product = productRepository.findById(reqProductReviewDto.getProduct_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND));
        ProductReview productReview = ProductReview.builder()
                .user(user).product(product)
                .content(reqProductReviewDto.getContent()).star(reqProductReviewDto.getStar()).build();
        productReviewRepository.save(productReview);
    }

    // 특정 상품의 모든 리뷰 최신순 조회(페이징)
    public Page<ResProductReviewListDto> getReviewByProductIdLatest(Long productId, Integer startOffset) {
        if(startOffset == null) {
            startOffset = 0;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(startOffset, PRODUCT_REVIEW_PAGE_SIZE, sort);
        Page<ProductReview> result = productReviewRepository.findActiveByProductId(productId, pageable);
        if(result.isEmpty()) {
            throw new ApplicationException(ApplicationError.REVIEW_NOT_FOUND);
        }
        return result.map(ResProductReviewListDto::productReviewFrom);
    }

    // 리뷰 수정
    public void updateReview(Long userId, UpdateProductReviewDto updateProductReviewDto) {
        ProductReview productReview = productReviewRepository.findByIdAndUserId(updateProductReviewDto.getId(), userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.REVIEW_NOT_FOUND));
        if(updateProductReviewDto.getContent() != null) {
            productReview.updateContent(updateProductReviewDto.getContent());
        }
        if(updateProductReviewDto.getStar() != null) {
            productReview.updateStar(updateProductReviewDto.getStar());
        }
        productReviewRepository.save(productReview);
    }

    // 리뷰 삭제
    public void deleteReview(Long userId, Long productReviewId) {
        ProductReview productReview = productReviewRepository.findByIdAndUserId(productReviewId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.REVIEW_NOT_FOUND));
        productReview.setDeleted();
        productReviewRepository.save(productReview);
    }

    // 리뷰 복구
    public void restoreReview(Long userId, Long productReviewId) {
        ProductReview productReview = productReviewRepository.findByIdAndUserId(productReviewId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.REVIEW_NOT_FOUND));
        productReview.unSetDeleted();
        productReviewRepository.save(productReview);
    }
}
