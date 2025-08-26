package com.example.monghyang.domain.brewery.tag;

import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.tag.entity.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BreweryTagRepository extends JpaRepository<BreweryTag,Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from BreweryTag b where b.brewery.id = :breweryId and b.tags.id = :tagId")
    void deleteByBreweryIdAndTagId(@Param("breweryId") Long breweryId, @Param("tagId") Integer tagId);
}
