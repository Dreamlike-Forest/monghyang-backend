package com.example.monghyang.domain.product.repository;

import com.example.monghyang.domain.product.dto.ResProductImageDto;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);


    @Query("select new com.example.monghyang.domain.product.dto.ResProductImageDto(pi.imageKey, pi.seq) from ProductImage pi where pi.product.id = :productId")
    List<ResProductImageDto> findSimpleByProductId(@Param("productId") Long productId);
}
