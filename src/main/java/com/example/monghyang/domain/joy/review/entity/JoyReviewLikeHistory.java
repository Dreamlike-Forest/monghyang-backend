package com.example.monghyang.domain.joy.review.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_user_id_joy_review_id",
                columnNames = {"user_id", "joy_review_id"}
        )
    }
)
public class JoyReviewLikeHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;
    @JoinColumn(name = "joy_review_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private JoyReview review;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    private JoyReviewLikeHistory(Users user, JoyReview review) {
        this.user = user;
        this.review = review;
    }

    public static JoyReviewLikeHistory userJoyReviewOf(Users user, JoyReview review) {
        return new JoyReviewLikeHistory(user, review);
    }
}
