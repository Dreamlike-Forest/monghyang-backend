package com.example.monghyang.domain.product.tag;

import com.example.monghyang.domain.tag.dto.TagNameDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProductTag p where p.product.id = :productId and p.tags.id in :deleteTagIdList")
    void deleteByProductIdAndTagId(@Param("productId") Long productId, @Param("deleteTagIdList") List<Integer> deleteTagIdList);

    @Query("select p from ProductTag p where p.product.id = :productId")
    List<ProductTag> findByProductId(@Param("productId") Long productId);

    /**
     * 특정 n개 상품이 가진 '인증' 태그 리스트 조회
     * @param productIdList
     * @return 인증 태그 리스트
     */
    @Query("select new com.example.monghyang.domain.tag.dto.TagNameDto(pt.product.id, pt.tags.name) from ProductTag pt where pt.product.id in :productIdList and pt.tags.category.id = 2")
    List<TagNameDto> findAuthTagListByProductIdList(@Param("productIdList") List<Long> productIdList);

    @Query("select pt.tags.name from ProductTag pt where pt.product.id = :productId")
    List<String> findTagListByProductId(@Param("productId") Long productId);
}
