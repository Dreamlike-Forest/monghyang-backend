package com.example.monghyang.domain.qna.entity;

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
public class QnaAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QNA_ANSWER_ID")
    private Long id;

    @JoinColumn(name = "QNA_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Qna qna;

    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private QnaAnswer(Qna qna, Users user, String content) {
        this.qna = qna;
        this.user = user;
        this.content = content;
    }

    public static QnaAnswer of(Qna qna, Users user, String content) {
        return new QnaAnswer(qna, user, content);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
