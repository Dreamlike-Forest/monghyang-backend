package com.example.monghyang.domain.product.repository;

import com.example.monghyang.domain.product.dto.ResProductListDto;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where p.id = :productId and p.user.id = :userId")
    Optional<Product> findByIdAndUserId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("select p from Product p where p.user.id = :userId")
    Optional<Product> findByUserId(@Param("userId") Long userId);

    @Query("""
    select new com.example.monghyang.domain.product.dto.ResProductListDto(p.id, u.name, p.name, avg(pr.star), count(pr.id), p.alcohol, p.volume, p.salesVolume, p.originPrice, p.discountRate, p.finalPrice, pi.imageKey)
        from Product p
        join Users u on p.user.id = u.id
        left join ProductReview pr on pr.product.id = p.id
        left join ProductImage pi on pi.product.id = p.id and pi.seq = 1
        where p.isDeleted = false
        group by p.id
    """)
    Page<ResProductListDto> findActiveLatest(Pageable pageable);

    @Query("""
    select new com.example.monghyang.domain.product.dto.ResProductListDto(p.id, u.name, p.name, avg(pr.star), count(pr.id), p.alcohol, p.volume, p.salesVolume, p.originPrice, p.discountRate, p.finalPrice, pi.imageKey)
        from Product p
        join Users u on p.user.id = u.id and u.id = :userId
        left join ProductReview pr on pr.product.id = p.id
        left join ProductImage pi on pi.product.id = p.id and pi.seq = 1
        where p.isDeleted = false
        group by p.id
    """)
    Page<ResProductListDto> findActiveByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        select new com.example.monghyang.domain.product.dto.ResProductListDto(p.id, u.name, p.name, avg(pr.star), count(pr.id), p.alcohol, p.volume, p.salesVolume, p.originPrice, p.discountRate, p.finalPrice, pi.imageKey)
        from Product p
        join p.user u on p.user.id = u.id
        left join ProductReview pr on pr.product.id = p.id
        left join ProductImage pi on pi.product.id = p.id and pi.seq = 1
        where
            (:tagListIsEmpty = true or exists (
                select 1
                from ProductTag pt where pt.product = p and pt.tags.id in (:tagIdList)
            ))
            and (:keyword is null or p.name like concat('%', :keyword, '%'))
            and (:minPrice is null or p.finalPrice >= :minPrice)
            and (:maxPrice is null or p.finalPrice <= :maxPrice)
            and (:minAlcohol is null or p.alcohol >= :minAlcohol)
            and (:maxAlcohol is null or p.alcohol <= :maxAlcohol)
            and p.isDeleted = false
        group by p.id
    """)
    Page<ResProductListDto> findByDynamicFiltering(Pageable pageable, @Param("tagListIsEmpty") boolean tagListIsEmpty, @Param("keyword") String keyword,
                                                   @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice,
                                                   @Param("minAlcohol") Double minAlcohol, @Param("maxAlcohol") Double maxAlcohol,
                                                   @Param("tagIdList") List<Integer> tagIdList);
}
