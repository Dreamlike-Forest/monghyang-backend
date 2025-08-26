package com.example.monghyang.domain.brewery.tag;

import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.tag.entity.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BreweryTagRepository extends JpaRepository<BreweryTag,Long> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from BreweryTag b where b.brewery.id = :breweryId and b.tags.id in :deleteTagIdList")
    void deleteByBreweryIdAndTagId(@Param("breweryId") Long breweryId, @Param("deleteTagIdList") List<Integer> deleteTagIdList);
}
