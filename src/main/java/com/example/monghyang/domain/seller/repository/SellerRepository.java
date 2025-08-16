package com.example.monghyang.domain.seller.repository;

import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUser(Users user);

    @Query("select s.user.phone from Seller s where s.id = :sellerId")
    Optional<String> findPhoneById(@Param("sellerId") Long sellerId);
}
