package com.example.monghyang.domain.tag.repository;

import com.example.monghyang.domain.tag.entity.TagCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagCategoryRepository extends JpaRepository<TagCategory,Integer> {

    @Query("select t from TagCategory t where t.name like %:keyword% and t.isDeleted = false")
    Page<TagCategory> findByKeywordActivePaging(@Param("keyword") String keyword, Pageable pageable);

    @Query("select t from TagCategory t where t.isDeleted = false")
    Page<TagCategory> findActivePaging(Pageable pageable);
}
