package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.entity.JoyWeeklyStartTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 요일별 체험 일정 시작 시간대(스냅샷 기반)를 조회하는 리포지토리 인터페이스입니다.
 */
public interface JoyWeeklyStartTimeRepository extends JpaRepository<JoyWeeklyStartTime, Long> {
    /**
     * 특정 체험 식별자의 특정 월 범위(시작일 기준 활성 스냅샷 ~ 종료일 이전 스냅샷) 동안 유효한 요일별 시작 시간대 스케줄 목록을 조회합니다.
     *
     * @param joyId     체험 식별자
     * @param startDate 조회 시작일 (해당 월 1일)
     * @param endDate   조회 종료일 (다음 월 1일, 미포함)
     * @return 월 범위 내 유효한 요일별 체험 시작 시간대 목록
     */
    @Query("""
        select jwst from JoyWeeklyStartTime jwst
        where jwst.joy.id = :joyId
          and jwst.effectiveDate < :endDate
          and jwst.effectiveDate >= coalesce(
              (select max(jwst2.effectiveDate)
               from JoyWeeklyStartTime jwst2
               where jwst2.joy.id = :joyId
                 and jwst2.effectiveDate <= :startDate),
              jwst.effectiveDate
          )
    """)
    List<JoyWeeklyStartTime> findActiveAndFutureStartTimesInMonth(
            @Param("joyId") Long joyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
