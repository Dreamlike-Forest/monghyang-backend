package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.brewery.dto.ResBreweryImageDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BreweryImageRepository extends JpaRepository<BreweryImage, Long> {

    List<BreweryImage> findByBrewery(Brewery brewery); // 특정 양조장이 업로드한 이미지 정보 조회

    @Query("select new com.example.monghyang.domain.brewery.dto.ResBreweryImageDto(b.id, b.imageKey, b.seq) from BreweryImage b where b.brewery.id = :breweryId")
    List<ResBreweryImageDto> findImageKeyByBrewery(@Param("breweryId") Long breweryId);

    @Modifying
    @Query("update BreweryImage bi set bi.seq = bi.seq * -1 where bi.brewery.id = :breweryId and bi.seq < 0")
    void updateImageSeqToPositive(@Param("breweryId") Long breweryId);

}
