package com.example.monghyang.domain.joy.review.dto;

import com.example.monghyang.domain.joy.review.entity.JoyReview;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ResJoyReviewDto {
    private Long joy_review_id;
    private Long joy_id;
    private String joy_name;
    private Long user_id;
    private String user_nickname;
    private String joy_review_content;
    private Double joy_review_star;
    private Integer joy_review_likes;
    private LocalDateTime joy_review_created_at;

    private ResJoyReviewDto(JoyReview joyReview) {
        this.joy_review_id = joyReview.getId();
        this.joy_id = joyReview.getJoy().getId();
        this.joy_name = joyReview.getJoy().getName();
        this.user_id = joyReview.getUser().getId();
        this.user_nickname = joyReview.getUser().getNickname();
        this.joy_review_content = joyReview.getContent();
        this.joy_review_star = joyReview.getStar();
        this.joy_review_likes = joyReview.getLikes();
        this.joy_review_created_at = joyReview.getCreatedAt();
    }

    /**
     * @param joyReview Users, Joy와 fetch join된 JoyReview Entity
     * @return
     */
    public static ResJoyReviewDto userAndJoyJoinedJoyReviewFrom(JoyReview joyReview) {
        return new ResJoyReviewDto(joyReview);
    }
}
