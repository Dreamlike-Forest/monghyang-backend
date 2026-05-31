package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.brewery.entity.BreweryWeeklyOpenTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BreweryWeeklyOpenTimeRepository extends JpaRepository<BreweryWeeklyOpenTime, Long> {

    /**
     * 특정 양조장의 특정 월 범위(시작일 기준 활성 스냅샷 ~ 종료일 이전 스냅샷) 동안 유효한 요일별 운영 시간대(스냅샷) 목록을 조회합니다.
     *
     * @param breweryId 양조장 식별자
     * @param startDate 조회 시작일 (해당 월 1일)
     * @param endDate   조회 종료일 (다음 월 1일, 미포함)
     * @return 월 범위 내 유효한 양조장 요일별 운영 시간대 목록
     */
    @Query("""
        select wot from BreweryWeeklyOpenTime wot
        where wot.brewery.id = :breweryId
          and wot.effectiveDate < :endDate
          and wot.effectiveDate >= coalesce(
              (select max(wot2.effectiveDate)
               from BreweryWeeklyOpenTime wot2
               where wot2.brewery.id = :breweryId
                 and wot2.effectiveDate <= :startDate),
              wot.effectiveDate
          )
    """)
    List<BreweryWeeklyOpenTime> findActiveAndFutureOpenTimesInMonth(
            @Param("breweryId") Long breweryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

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
