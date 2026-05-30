package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.brewery.entity.BreweryClosedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.example.monghyang.domain.global.ClosedStatus;

public interface BreweryClosedDateRepository extends JpaRepository<BreweryClosedDate,Long> {
    @Modifying
    @Query("delete from BreweryClosedDate bcd where bcd.brewery.id = :breweryId and bcd.closedDate = :closedDate")
    int deleteByBreweryIdAndClosedDate(@Param("breweryId") Long breweryId, @Param("closedDate") LocalDate closedDate);

    Optional<BreweryClosedDate> findByBreweryIdAndClosedDate(Long breweryId, LocalDate closedDate);

    /**
     * 특정 양조장의 특정 기간(월 범위) 내에 확정된 임시 휴무일 목록을 조회합니다.
     *
     * @param breweryId 양조장 식별자
     * @param startDate 조회 시작 날짜
     * @param endDate   조회 종료 날짜 (미포함)
     * @param status    임시 휴무 확정 상태
     * @return 확정된 양조장 임시 휴무일 목록
     */
    @Query("""
        select bcd from BreweryClosedDate bcd
        where bcd.brewery.id = :breweryId
          and bcd.closedDate >= :startDate
          and bcd.closedDate < :endDate
          and bcd.closedStatus = :status
    """)
    List<BreweryClosedDate> findConfirmedByBreweryIdAndMonth(
            @Param("breweryId") Long breweryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ClosedStatus status
    );
}
