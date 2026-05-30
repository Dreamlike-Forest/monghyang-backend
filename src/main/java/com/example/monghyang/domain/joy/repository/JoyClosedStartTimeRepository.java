package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.entity.JoyClosedStartTime;
import com.example.monghyang.domain.global.ClosedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 특정 날짜 체험의 시간대별 임시 휴무 일정 시작 시간대 정보를 조회하는 리포지토리 인터페이스입니다.
 */
public interface JoyClosedStartTimeRepository extends JpaRepository<JoyClosedStartTime, Long> {
    /**
     * 특정 체험에 대해 특정 기간(월 범위) 내에 확정된 시간대별 임시 휴무 일정 목록을 조회합니다.
     * 상위 휴일 정보와 하위 시간대 정보가 모두 확정(CONFIRMED) 상태인 것들만 가져옵니다.
     *
     * @param joyId     체험 식별자
     * @param startDate 조회 시작 날짜
     * @param endDate   조회 종료 날짜 (미포함)
     * @param status    임시 휴무 확정 상태
     * @return 확정된 시간대별 임시 휴무 목록
     */
    @Query("""
        select jcst from JoyClosedStartTime jcst
        join jcst.joyClosedDate jcd
        where jcd.joy.id = :joyId
          and jcd.closedDate >= :startDate
          and jcd.closedDate < :endDate
          and jcd.closedStatus = :status
          and jcst.closedStatus = :status
    """)
    List<JoyClosedStartTime> findConfirmedByJoyIdAndMonth(
            @Param("joyId") Long joyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ClosedStatus status
    );
}
