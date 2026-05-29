package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.brewery.entity.BreweryWeeklyOpenTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BreweryWeeklyOpenTimeRepository extends JpaRepository<BreweryWeeklyOpenTime, Long> {

    /**
     * 특정 양조장의 특정 effective_date에 해당하는 운영시간 레코드를 삭제합니다.
     * 동일 effective_date로 스케줄을 재등록할 때 기존 레코드를 먼저 제거하기 위해 사용합니다.
     *
     * @param breweryId     양조장 식별자
     * @param effectiveDate 삭제 대상 적용 시작일
     */
    @Modifying
    @Query("delete from BreweryWeeklyOpenTime b where b.brewery.id = :breweryId and b.effectiveDate = :effectiveDate")
    void deleteByBreweryIdAndEffectiveDate(
            @Param("breweryId") Long breweryId,
            @Param("effectiveDate") LocalDate effectiveDate
    );
}
