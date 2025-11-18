package com.example.monghyang.domain.product.tag;

import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.tag.entity.Tags;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_product_tag",
                columnNames = {"PRODUCT_ID", "TAG_ID"} // 상품 식별자와 태그 식별자를 묶어서 유니크하게 설정
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductTag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    @JoinColumn(name = "TAG_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Tags tags;

    private ProductTag(Product product, Tags tags) {
        this.product = product;
        this.tags = tags;
    }
    public static ProductTag productTagsOf(Product product, Tags tags) {
        return new ProductTag(product, tags);
    }
}
