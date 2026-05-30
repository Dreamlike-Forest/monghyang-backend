package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.dto.JoyScheduleCountDto;
import com.example.monghyang.domain.joy.dto.slot.UnavailableJoySlotTimeCountDto;
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
     * 예약 슬롯 upsert (native query)
     */
    @Modifying
    @Query(value = """
        insert into joy_slot(joy_id, reservation_date, reservation_time, count)
        values(:joyId, :date, :time, :incCount)
        on duplicate key update
            `count` = if(
                `count` + :incCount <= :maxCount,
                `count` + :incCount,
                `count`
            );
    """, nativeQuery = true)
    int upsertJoySlot(@Param("joyId") Long joyId,
                      @Param("date") LocalDate date,
                      @Param("time") LocalTime time,
                      @Param("incCount") Integer incCount,
                      @Param("maxCount") Integer maxCount
    );

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
    and js.count - :count >= 0
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

    @Query("select js from JoySlot js where js.joy.id = :joyId and js.reservationDate = :date")
    List<JoySlot> findByJoyIdAndDate(@Param("joyId") Long joyId, @Param("date") LocalDate date);
}
