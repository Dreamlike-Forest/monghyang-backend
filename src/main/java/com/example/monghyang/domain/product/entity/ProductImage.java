package com.example.monghyang.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_product_image_seq",
                columnNames = {"PRODUCT_ID", "SEQ"} // 상품 식별자와 이미지 순서 정보를 묶어서 유니크 설정
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    @Column(nullable = false, unique = true)
    private String imageKey; // UUID + 확장자명까지 포함한 이미지 전체 이름
    @Column(nullable = false)
    private Integer seq;
    @Column(nullable = false)
    private Long volume;

    private ProductImage(Product product, String imageKey, Integer seq, Long volume) {
        this.product = product;
        this.imageKey = imageKey;
        this.seq = seq;
        this.volume = volume;
    }

    /**
     * ProductImage 엔티티를 생성하고 반환합니다.
     * @param product 상품 엔티티
     * @param imageKey 이미지의 키
     * @param seq 이미지 순서
     * @param volume 이미지 용량
     * @return
     */
    public static ProductImage productImageKeySeqVolumeOf(Product product, String imageKey, Integer seq, Long volume) {
        return new ProductImage(product, imageKey, seq, volume);
    }

    public void updateSeq(Integer seq) {
        this.seq = seq;
    }
}
