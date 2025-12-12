package com.example.monghyang.domain.community.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"FOLLOWER_ID", "FOLLOWING_ID"})
})
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FOLLOW_ID")
    private Long id;

    @JoinColumn(name = "FOLLOWER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users follower;  // 팔로우 하는 사람

    @JoinColumn(name = "FOLLOWING_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users following;  // 팔로우 받는 사람

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public Follow(Users follower, Users following) {
        this.follower = follower;
        this.following = following;
    }
}
