package com.example.monghyang.domain.joy.review.repository;

import com.example.monghyang.domain.joy.review.entity.JoyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JoyReviewRepository extends JpaRepository<JoyReview, Long> {
    @Modifying
    @Query("update JoyReview jr set jr.likes = jr.likes + 1 where jr.id = :joyReviewId")
    int increaseLike(@Param("joyReviewId") Long joyReviewId);

    @Modifying
    @Query("update JoyReview jr set jr.likes = jr.likes - 1 where jr.id = :joyReviewId")
    int decreaseLike(@Param("joyReviewId") Long joyReviewId);
}
