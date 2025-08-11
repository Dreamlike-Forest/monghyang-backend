package com.example.monghyang.domain.seller.repository;

import com.example.monghyang.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {
}
