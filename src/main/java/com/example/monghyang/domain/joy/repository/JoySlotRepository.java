package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.entity.JoySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
}
