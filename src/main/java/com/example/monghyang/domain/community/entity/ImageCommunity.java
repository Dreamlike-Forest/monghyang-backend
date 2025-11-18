package com.example.monghyang.domain.community.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageCommunity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IMAGE_COMMUNITY_ID")
    private Long id;

    @JoinColumn(name = "COMMUNITY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Community community;

    @Column(nullable = false)
    private Integer imageNum;

    @Column(nullable = false)
    private String imageUrl;

    @Builder
    public ImageCommunity(Community community, Integer imageNum, String imageUrl) {
        this.community = community;
        this.imageNum = imageNum;
        this.imageUrl = imageUrl;
    }

    public void updateImageNum(Integer imageNum) {
        this.imageNum = imageNum;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
