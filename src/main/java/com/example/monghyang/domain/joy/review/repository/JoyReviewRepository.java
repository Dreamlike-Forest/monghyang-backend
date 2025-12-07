package com.example.monghyang.domain.joy.review.repository;

import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.review.entity.JoyReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * 특정 양조장의 모든 체험의 댓글형 리뷰 최신순 조회
     *
     * @param pageable
     * @param breweryId 양조장 식별자
     * @return
     */
    @Query("select jr from JoyReview jr join fetch jr.user u join fetch jr.joy j where j.brewery.id = :breweryId order by jr.createdAt desc")
    Page<JoyReview> findLatestByBrewery(Pageable pageable, @Param("breweryId") Long breweryId);

    /**
     * 특정 양조장의 모든 체험의 댓글형 리뷰 좋아요 수 내림차순 조회(동일 좋아요 수 레코드는 최신순 정렬)
     *
     * @param pageable
     * @param breweryId 양조장 식별자
     * @return
     */
    @Query("select jr from JoyReview jr join fetch jr.user u join fetch jr.joy j where j.brewery.id = :breweryId order by jr.likes desc, jr.createdAt desc")
    Page<JoyReview> findLikesDescByBrewery(Pageable pageable, @Param("breweryId") Long breweryId);

    /**
     * 특정 양조장의 모든 체험의 댓글형 리뷰 별점 내림차순 조회(동일 별점 레코드는 최신순 정렬)
     *
     * @param pageable
     * @param breweryId 양조장 식별자
     * @return
     */
    @Query("select jr from JoyReview jr join fetch jr.user u join fetch jr.joy j where j.brewery.id = :breweryId order by jr.star desc, jr.createdAt desc")
    Page<JoyReview> findStarDescByBrewery(Pageable pageable, @Param("breweryId") Long breweryId);

    /**
     * 특정 체험의 모든 댓글형 리뷰 최신순 조회
     *
     * @param pageable
     * @param joyId    체험 식별자
     * @return
     */
    @Query("select jr from JoyReview jr join fetch jr.user u join fetch jr.joy j where j.id = :joyId order by jr.createdAt desc")
    Page<JoyReview> findLatestByJoy(Pageable pageable, @Param("joyId") Long joyId);

    /**
     * 특정 체험의 모든 댓글형 리뷰 좋아요 많은 순 조회
     *
     * @param pageable
     * @param joyId    체험 식별자
     * @return
     */
    @Query("select jr from JoyReview jr join fetch jr.user u join fetch jr.joy j where j.id = :joyId order by jr.likes desc, jr.createdAt desc")
    Page<JoyReview> findLikesDescByJoy(Pageable pageable, @Param("joyId") Long joyId);

    /**
     * 특정 체험의 모든 댓글형 리뷰 별점 높은 순 조회
     *
     * @param pageable
     * @param joyId    체험 식별자
     * @return
     */
    @Query("select jr from JoyReview jr join fetch jr.user u join fetch jr.joy j where j.id = :joyId order by jr.star desc, jr.createdAt desc")
    Page<JoyReview> findStarDescByJoy(Pageable pageable, @Param("joyId") Long joyId);

}
