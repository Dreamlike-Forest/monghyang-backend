package com.example.monghyang.domain.tag.repository;

import com.example.monghyang.domain.tag.entity.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagsRepository extends JpaRepository<Tags, Integer> {

    @Query("select t from Tags t join fetch t.category where t.name like %:keyword% and t.isDeleted = false")
    Page<Tags> findByKeywordActivePaging(@Param("keyword") String keyword, Pageable pageable);
    @Query("select t from Tags t join fetch t.category where t.isDeleted = false")
    Page<Tags> findActivePaging(Pageable pageable);
}
