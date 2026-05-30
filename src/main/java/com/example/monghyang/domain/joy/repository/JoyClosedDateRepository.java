package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.entity.JoyClosedDate;
import com.example.monghyang.domain.global.ClosedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 체험의 임시 휴무일 정보를 조회하는 리포지토리 인터페이스입니다.
 */
public interface JoyClosedDateRepository extends JpaRepository<JoyClosedDate, Long> {
    /**
     * 특정 체험에 대해 특정 기간(월 범위) 내에 확정된 임시 휴무일 목록을 조회합니다.
     *
     * @param joyId     체험 식별자
     * @param startDate 조회 시작 날짜
     * @param endDate   조회 종료 날짜 (미포함)
     * @param status    임시 휴무일 확정 상태
     * @return 확정된 체험 임시 휴무일 목록
     */
    @Query("""
        select jcd from JoyClosedDate jcd
        where jcd.joy.id = :joyId
          and jcd.closedDate >= :startDate
          and jcd.closedDate < :endDate
          and jcd.closedStatus = :status
    """)
    List<JoyClosedDate> findConfirmedByJoyIdAndMonth(
            @Param("joyId") Long joyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ClosedStatus status
    );
}
