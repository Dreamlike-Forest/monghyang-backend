package com.example.monghyang.domain.seller.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_seller_image_seq",
                columnNames = {"SELLER_ID", "SEQ"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerImage {
    @Id @GeneratedValue
    private Long id;
    @JoinColumn(name = "SELLER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Seller seller;
    @Column(nullable = false)
    private String imageKey; // UUID + 확장자명까지 포함한 이미지 전체 이름
    @Column(nullable = false)
    private Integer seq;
    @Column(nullable = false)
    private Long volume;

    private SellerImage(Seller seller, String imageKey, Integer seq, Long volume) {
        this.seller = seller;
        this.imageKey = imageKey;
        this.seq = seq;
        this.volume = volume;
    }

    /**
     * SellerImage 엔티티를 생성하고 반환합니다.
     * @param seller 판매자 Entity
     * @param imageKey 이미지 업로드 후 반환된 UUID 값
     * @param seq 해당 이미지의 순서 정보
     * @param volume 해당 이미지의 용량 정보
     * @return
     */
    public static SellerImage sellerKeySeqVolume(Seller seller, String imageKey, Integer seq, Long volume) {
        return new SellerImage(seller, imageKey, seq, volume);
    }

    public void updateSeq(Integer seq) {
        this.seq = seq;
    }
}
