package com.example.monghyang.domain.cart.entity;

import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Check(constraints = "quantity BETWEEN 1 AND 99")
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    // 수량은 1 ~ 99 사이의 값만 입력될 수 있습니다.
    @Column(nullable = false)
    @Min(value = 1, message = "장바구니 수량은 1 이상이어야 합니다.")
    @Max(value = 99, message = "장바구니 수량은 100보다 작아야 합니다.")
    private Integer quantity;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Cart(Users user, Product product, Integer quantity) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
    }
    public static Cart userProductQuantityOf(Users user, Product product, Integer quantity) {
        return new Cart(user, product, quantity);
    }

    public void plusOneQuantity() {
        this.quantity++;
    }
    public void minusOneQuantity() {
        this.quantity--;
    }

    public void updateSpecifiedQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
