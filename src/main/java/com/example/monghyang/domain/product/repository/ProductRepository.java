package com.example.monghyang.domain.product.repository;

import com.example.monghyang.domain.product.dto.ResProductListDto;
import com.example.monghyang.domain.product.entity.Product;
import com.example.monghyang.domain.users.entity.Users;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where p.id = :productId and p.user.id = :userId")
    Optional<Product> findByIdAndUserId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("select p from Product p where p.id = :productId and p.user.id = :userId")
    Optional<Product> findByUserId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("""
    select new com.example.monghyang.domain.product.dto.ResProductListDto(p.id, u.name, p.name, avg(pr.star), count(pr.id), p.alcohol, p.volume, p.salesVolume, p.originPrice, p.discountRate, p.finalPrice, pi.imageKey, p.isOnlineSell, p.isSoldout)
        from Product p
        join Users u on p.user.id = u.id
        left join ProductReview pr on pr.product.id = p.id
        left join ProductImage pi on pi.product.id = p.id and pi.seq = 1
        where p.isOnlineSell = true and p.isDeleted = false
        group by p.id
    """)
    Page<ResProductListDto> findActiveLatest(Pageable pageable); // 상품 리스트 최신순 조회

    @Query("""
    select new com.example.monghyang.domain.product.dto.ResProductListDto(p.id, u.name, p.name, avg(pr.star), count(pr.id), p.alcohol, p.volume, p.salesVolume, p.originPrice, p.discountRate, p.finalPrice, pi.imageKey, p.isOnlineSell, p.isSoldout)
        from Product p
        join Users u on p.user.id = u.id and u.id = :userId
        left join ProductReview pr on pr.product.id = p.id
        left join ProductImage pi on pi.product.id = p.id and pi.seq = 1
        where p.isDeleted = false
        group by p.id
    """)
    Page<ResProductListDto> findActiveByUserId(@Param("userId") Long userId, Pageable pageable); // 특정 유저가 업로드한 상품 조회


    /**
     * 메소드 호출 시 모든 파라메터는 반드시 전달되어야 합니다. 적용되지 않는 필터링 요소에 대해서는 null 값을 대입하여 넘겨주세요.
     * @param pageable 페이징 크기 및 기준을 의미하는 인스턴스
     * @param tagListIsEmpty 태그 기준 필터링이 적용되었는지 여부를 나타내는 플래그 변수
     * @param keyword 양조장 이름 키워드 필터링 조건
     * @param minPrice 최소 체험 가격 필터링 조건
     * @param maxPrice 최대 체험 가격 필터링 조건
     * @param minAlcohol 최소 도수 필터링 조건
     * @param maxAlcohol 최대 도수 필터링 조건
     * @param tagIdList 태그 식별자 리스트 필터링 조건
     */
    @Query("""
        select new com.example.monghyang.domain.product.dto.ResProductListDto(p.id, u.name, p.name, avg(pr.star), count(pr.id), p.alcohol, p.volume, p.salesVolume, p.originPrice, p.discountRate, p.finalPrice, pi.imageKey, p.isOnlineSell, p.isSoldout)
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
            and p.isOnlineSell = true and p.isDeleted = false
        group by p.id
    """)
    Page<ResProductListDto> findByDynamicFiltering(Pageable pageable, @Param("tagListIsEmpty") boolean tagListIsEmpty, @Param("keyword") String keyword,
                                                   @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice,
                                                   @Param("minAlcohol") Double minAlcohol, @Param("maxAlcohol") Double maxAlcohol,
                                                   @Param("tagIdList") List<Integer> tagIdList); // 상품 필터링 조회

    /**
     * 상품 재고 증가. 게시자만 수행 가능
     * @param productId
     * @param userId
     * @param quantity
     */
    @Modifying
    @Query("update Product p set p.inventory = p.inventory + :quantity where p.id = :productId and p.user.id = :userId")
    int increseInventory(@Param("productId") Long productId, @Param("userId") Long userId, @Param("quantity") Integer quantity);

    /**
     * 상품 재고 감소(출고 및 구매 등). 소유권 검증 X
     * @param productId
     * @param quantity
     */
    @Modifying
    @Query("update Product p set p.inventory = p.inventory - :quantity where p.id = :productId")
    void decreseInventory(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 주문 시 사용되는 상품 재고 수량 일괄 감소 쿼리문(native). 삭제되지 않고 품절되지 않은 상품만 재고 차감.
     * @param productIds 구매할 장바구니 식별자 리스트
     * @return
     */
    @Modifying
    @Query(value = """
    update cart c
    join product p on c.product_id = p.id
    set p.inventory = p.inventory - c.quantity
    where p.id in :productIds and p.is_deleted = false and p.is_soldout = false
    """, nativeQuery = true)
    int decreaseInventoryForOrderByProductIds(@Param("productIds") List<Long> productIds);

    /**
     * 주문 프로세스 실패 시 실행되는 보상 트랜잭션에서 수행: 차감했던 재고 복구
     * @param productIds
     * @return
     */
    @Modifying
    @Query(value = """
    update cart c
    join product p on c.product_id = p.id
    set p.inventory = p.inventory + c.quantity
    where p.id in :productIds
    """, nativeQuery = true)
    void increaseInventoryForOrderByProductIds(@Param("productIds") List<Long> productIds);
}
