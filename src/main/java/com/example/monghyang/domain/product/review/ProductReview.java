package com.example.monghyang.domain.product.review;

import com.example.monghyang.domain.product.entity.Product;
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
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_product_user",
                columnNames = {"PRODUCT_ID", "USER_ID"} // 상품 식별자와 회원 식별자를 묶어서 유니크하게 설정
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    @Column(nullable = false)
    private String content;
    @Column(nullable = false)
    private Double star;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted;

    @Builder
    public ProductReview(Product product, Users user, String content, Double star) {
        this.product = product;
        this.user = user;
        this.content = content;
        if(star < 0.5) {
            star = 0.5;
        } else if(star > 5.0) {
            star = 5.0;
        }
        this.star = star;
        this.isDeleted = false;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStar(Double star) {
        if(star < 0.5) {
            star = 0.5;
        } else if(star > 5.0) {
            star = 5.0;
        }
        this.star = star;
    }

    public void setDeleted() {
        this.isDeleted = Boolean.TRUE;
    }

    public void unSetDeleted() {
        this.isDeleted = Boolean.FALSE;
    }
}
