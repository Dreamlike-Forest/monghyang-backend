package com.example.monghyang.domain.cart.repository;

import com.example.monghyang.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("select c from Cart c where c.product.id = :productId and c.user.id = :userId")
    Optional<Cart> findByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    @Query("select c from Cart c where c.id = :id and c.user.id = :userId")
    Optional<Cart> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("select c from Cart c where c.user.id = :userId")
    List<Cart> findByUserId(@Param("userId") Long userId);

    // 해당 유저의 장바구니의 주문 목록 중 '삭제 처리되지 않은' 요소를 조회
    @Query("select c from Cart c join fetch c.product p where c.id in :idList and c.user.id = :userId and p.isDeleted = false")
    List<Cart> findByIdListAndUserId(@Param("idList") List<Long> idList, @Param("userId") Long userId);
}
