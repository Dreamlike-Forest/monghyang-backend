package com.example.monghyang.domain.product.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    @Query("select pr from ProductReview pr where pr.id = :productReviewId and pr.user.id = :userId")
    Optional<ProductReview> findByIdAndUserId(@Param("productReviewId") Long productReviewId, @Param("userId") Long userId);

    @Query("select pr from ProductReview pr join fetch pr.user where pr.product.id = :productId and pr.isDeleted = false")
    Page<ProductReview> findActiveByProductId(Long productId, Pageable pageable);
}
