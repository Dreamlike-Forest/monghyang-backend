package com.example.monghyang.domain.product.repository;

import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);
}
