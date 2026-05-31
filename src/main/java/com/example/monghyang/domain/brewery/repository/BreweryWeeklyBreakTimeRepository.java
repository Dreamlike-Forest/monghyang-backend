package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.global.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BreweryWeeklyBreakTimeRepository extends JpaRepository<BreweryWeeklyBreakTime, Long> {

    /**
     * 예약일 기준 최신 양조장 주간 운영시간 스냅샷 버전을 먼저 선택한 뒤, 그 버전 안의 휴게시간을 조회합니다.
     * 최신 버전에 해당 요일 휴게시간 row가 없으면 과거 휴게시간을 되살리지 않고 빈 결과를 반환합니다.
     *
     * @param breweryId  양조장 식별자
     * @param targetDate 예약 대상일
     * @param dayOfWeek  예약 대상일의 요일
     * @return 예약일에 적용되는 양조장 휴게시간 목록
     */
    @Query("""
        select bbt from BreweryWeeklyBreakTime bbt
        where bbt.brewery.id = :breweryId
          and bbt.dayOfWeek = :dayOfWeek
          and bbt.effectiveDate = (
              select max(wot.effectiveDate)
              from BreweryWeeklyOpenTime wot
              where wot.brewery.id = :breweryId
                and wot.effectiveDate <= :targetDate
          )
    """)
    List<BreweryWeeklyBreakTime> findActiveBreakTimesByBreweryIdAndDate(
            @Param("breweryId") Long breweryId,
            @Param("targetDate") LocalDate targetDate,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    /**
     * 특정 월 범위에서 시작일 기준 활성 스냅샷부터 종료일 이전 스냅샷까지의 양조장 휴게시간 목록을 조회합니다.
     *
     * @param breweryId 양조장 식별자
     * @param startDate 조회 시작일
     * @param endDate   조회 종료일
     * @return 월 범위 계산에 필요한 양조장 휴게시간 목록
     */
    @Query("""
        select bbt from BreweryWeeklyBreakTime bbt
        where bbt.brewery.id = :breweryId
          and bbt.effectiveDate < :endDate
          and bbt.effectiveDate >= coalesce(
              (select max(bbt2.effectiveDate)
               from BreweryWeeklyBreakTime bbt2
               where bbt2.brewery.id = :breweryId
                 and bbt2.effectiveDate <= :startDate),
              bbt.effectiveDate
          )
    """)
    List<BreweryWeeklyBreakTime> findActiveAndFutureBreakTimesInMonth(
            @Param("breweryId") Long breweryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 양조장의 특정 effective_date에 해당하는 휴게시간 레코드를 삭제합니다.
     * 동일 effective_date로 스케줄을 재등록할 때 기존 레코드를 먼저 제거하기 위해 사용합니다.
     *
     * @param breweryId     양조장 식별자
     * @param effectiveDate 삭제 대상 적용 시작일
     */
    @Modifying
    @Query("delete from BreweryWeeklyBreakTime b where b.brewery.id = :breweryId and b.effectiveDate = :effectiveDate")
    void deleteByBreweryIdAndEffectiveDate(
            @Param("breweryId") Long breweryId,
            @Param("effectiveDate") LocalDate effectiveDate
    );
}
