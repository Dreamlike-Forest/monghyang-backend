package com.example.monghyang.domain.joy.review.repository;

import com.example.monghyang.domain.joy.review.entity.JoyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JoyReviewRepository extends JpaRepository<JoyReview, Long> {
    /**
     * 댓글형 리뷰의 조회수 1 증가
     * @param joyReviewId 리뷰 식별자
     * @return 조회수 증가된 레코드의 수
     */
    @Modifying
    @Query("update JoyReview jr set jr.view = jr.view + 1 where jr.id = :joyReviewId")
    int increaseView(@Param("joyReviewId") Long joyReviewId);
}
