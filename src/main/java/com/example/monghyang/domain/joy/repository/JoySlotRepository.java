package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.dto.JoyScheduleCountDto;
import com.example.monghyang.domain.joy.dto.UnavailableJoySlotTimeCountDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface JoySlotRepository extends JpaRepository<JoySlot, Integer> {
    /**
     * 예약 슬롯 새로 생성
     * @param joyId
     * @param date
     * @param time
     * @param count
     * @return
     */
    @Modifying
    @Query(value = """
        insert into joy_slot(joy_id, reservation_date, reservation_time, count)
        values(:joyId, :date, :time, :count);
    """, nativeQuery = true)
    int insertJoySlot(@Param("joyId") Long joyId, @Param("date") LocalDate date, @Param("time") LocalTime time, @Param("count") Integer count);

    /**
     * 기존 예약 슬롯의 카운트 증가
     * @param joyId
     * @param date
     * @param time
     * @param count
     * @return
     */
    @Modifying
    @Query("""
        update JoySlot js set js.count = js.count + :count
        where js.joy.id = :joyId and js.reservationDate = :date and js.reservationTime = :time
        and (js.count + :count) <= (select j.maxCount from Joy j where j.id = :joyId)
    """)
    int incrementJoySlotCount(@Param("joyId") Long joyId, @Param("date") LocalDate date, @Param("time") LocalTime time, @Param("count") Integer count);

    /**
     * 기존 예약 슬롯의 카운트 감소(감소 후 0 이하가 되는 경우 수정하지 않는다)
     * @param joyId
     * @param date
     * @param time
     * @param count
     * @return
     */
    @Modifying
    @Query("""
    update JoySlot js set js.count = js.count - :count
    where js.joy.id = :joyId and js.reservationDate = :date and js.reservationTime = :time
    and js.count - :count > 0
    """)
    int decrementJoySlotCount(@Param("joyId") Long joyId, @Param("date") LocalDate date, @Param("time") LocalTime time, @Param("count") Integer count);

    /**
     * 카운트가 0이 되는 슬롯의 레코드 제거
     * @param joyId
     * @param date
     * @param time
     * @return
     */
    @Modifying
    @Query("""
    delete from JoySlot js where js.joy.id = :joyId and js.reservationDate = :date and js.reservationTime = :time
    """)
    void deleteJoySlot(@Param("joyId") Long joyId, @Param("date") LocalDate date, @Param("time") LocalTime time);

    /**
     * 특정 체험이 하루에 몇 회 운영하는지 계산하기 위한 데이터 조회
     * @param joyId
     * @return 체험 시간 단위, 양조장 운영 시작 시간 및 종료 시간 정보
     */
    @Query("select j.timeUnit as timeUnit, b.startTime as startTime, b.endTime as endTime from Joy j join j.brewery b where j.id = :joyId")
    Optional<JoyScheduleCountDto> findJoyScheduleCountByJoyId(@Param("joyId") Long joyId);


    /**
     * 한달 동안 일별로 '예약 불가 시간대'의 개수를 조회
     * @param joyId
     * @param startDate 조회 기준 (yyyy - mm - 01)
     * @param endDate 다음달 1일 (yyyy - (mm+1) - 01)
     * @return
     */
    @Query("""
    select js.reservationDate reservationDate, count(*) as count from JoySlot js
    where js.joy.id = :joyId and js.reservationDate >= :startDate and js.reservationDate < :endDate
    and js.count >= (select j.maxCount from Joy j where j.id = :joyId)
    group by js.reservationDate
    """)
    List<UnavailableJoySlotTimeCountDto> findUnavailableJoySlotTimeCountByJoyIdAndMonth(@Param("joyId") Long joyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
