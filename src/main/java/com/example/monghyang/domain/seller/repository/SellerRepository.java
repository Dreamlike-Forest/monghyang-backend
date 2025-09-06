package com.example.monghyang.domain.seller.repository;

import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    @Query("select s from Seller s where s.user.id = :userId")
    Optional<Seller> findByUserId(@Param("userId") Long userId);

    @Query("select s from Seller s where s.sellerName like %:keyword% and s.isDeleted = false")
    Page<Seller> findActiveByNameKeyword(Pageable pageable, @Param("keyword") String keyword);

    @Query("select s from Seller s where s.isDeleted = false")
    Page<Seller> findActiveLatest(Pageable pageable);
}
