package com.example.monghyang.domain.joy.review.repository;

import com.example.monghyang.domain.joy.review.entity.JoyReviewLikeHistory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface JoyReviewLikeHistoryRepository extends CrudRepository<JoyReviewLikeHistory, Long> {
    @Modifying
    @Query(value = """
        insert joy_review_like_history(user_id, joy_review_id)
            values(:userId, :joyReviewId);
    """, nativeQuery = true)
    void insertJoyLikeHistory(@Param("userId") Long userId, @Param("joyReviewId") Long joyReviewId);

    @Modifying
    @Query("delete JoyReviewLikeHistory h where h.user.id = :userId and h.review.id = :joyReviewId")
    int deleteByUserIdAndJoyReviewId(Long userId, Long joyReviewId);
}
