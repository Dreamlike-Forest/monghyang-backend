package com.example.monghyang.domain.seller.repository;

import com.example.monghyang.domain.seller.dto.ResSellerImageDto;
import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.seller.entity.SellerImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerImageRepository extends JpaRepository<SellerImage, Long> {
    List<SellerImage> findBySeller(Seller seller);

    @Query("select new com.example.monghyang.domain.seller.dto.ResSellerImageDto(s.imageKey, s.seq) from SellerImage s where s.seller.id = :sellerId")
    List<ResSellerImageDto> findImageKeyBySeller(@Param("sellerId") Long sellerId);

    @Query("select s from SellerImage s where s.id = :id and s.seller.id = :sellerId")
    Optional<SellerImage> findByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);
}
