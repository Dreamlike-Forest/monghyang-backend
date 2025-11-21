package com.example.monghyang.domain.joy.review.entity;

import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoyReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;
    @JoinColumn(name = "JOY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Joy joy;
    @Column(nullable = false)
    private String content;
    @Column(nullable = false)
    private Double star;
    @Column(nullable = false)
    private Integer view;
    @Column(nullable = false)
    private Integer likes;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted = Boolean.FALSE;

    @Builder
    public JoyReview(@NonNull Users user, @NonNull Joy joy, @NonNull String content, @NonNull Double star) {
        this.user = user;
        this.joy = joy;
        this.content = content;
        this.star = star;
        this.view = 0;
        this.likes = 0;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStar(Double star) {
        this.star = star;
    }

    public void setDeleted() {
        this.isDeleted = true;
    }

    public void unSetDeleted() {
        this.isDeleted = false;
    }
}
