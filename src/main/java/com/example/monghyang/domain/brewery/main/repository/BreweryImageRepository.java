package com.example.monghyang.domain.brewery.main.repository;

import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.brewery.main.entity.BreweryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BreweryImageRepository extends JpaRepository<BreweryImage, Long> {

    List<BreweryImage> findByBrewery(Brewery brewery); // 특정 양조장이 업로드한 이미지 정보 조회

    @Query("select b from BreweryImage b where b.id = :id and b.brewery.id = :breweryId")
    Optional<BreweryImage> findByIdAndBreweryId(@Param("id") Long id, @Param("breweryId") Long breweryId);

}
