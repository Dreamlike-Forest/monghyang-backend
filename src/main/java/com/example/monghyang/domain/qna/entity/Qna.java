package com.example.monghyang.domain.qna.entity;

import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Qna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QNA_ID")
    private Long id;

    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    @Column(nullable = false)
    private String qnaTitle;

    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isComplete = Boolean.FALSE;

    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Builder
    public Qna(Users user, String qnaTitle, String content) {
        this.user = user;
        this.qnaTitle = qnaTitle;
        this.content = content;
    }

    public void updateQnaTitle(String qnaTitle) {
        this.qnaTitle = qnaTitle;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void setComplete() {
        this.isComplete = Boolean.TRUE;
    }

    public void setDeleted() {
        this.isDeleted = Boolean.TRUE;
    }

    public void unSetDeleted() {
        this.isDeleted = Boolean.FALSE;
    }
}
