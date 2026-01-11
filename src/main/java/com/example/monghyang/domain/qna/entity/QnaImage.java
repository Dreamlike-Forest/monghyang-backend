package com.example.monghyang.domain.qna.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QNA_IMAGE_ID")
    private Long id;

    @JoinColumn(name = "QNA_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Qna qna;

    @Column(nullable = false)
    private String imageKey;

    @Column(nullable = false)
    private Integer volume;

    @Builder
    public QnaImage(Qna qna, String imageKey, Integer volume) {
        this.qna = qna;
        this.imageKey = imageKey;
        this.volume = volume;
    }

    public void updateImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void updateVolume(Integer volume) {
        this.volume = volume;
    }
}
